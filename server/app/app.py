import os
import datetime
import logging
import flask
import json
import werkzeug
import optparse
import tornado.wsgi
import tornado.httpserver
import caffe
import imagenetclassifier as inc

UPLOAD_FOLDER = '/tmp/caffe_demos_uploads'
ALLOWED_IMAGE_EXTENSIONS = set(['PNG', 'BMP', 'JPG', 'JPE', 'JPEG', 'GIF'])

# Obtain the flask app object
app = flask.Flask(__name__)


@app.route('/', methods=['GET'])
def default_route():
    return "This is the default route for the image classification API"

@app.route('/classify_upload', methods=['POST'])
def classify_upload():
    try:
        imagefile = flask.request.files['image_file']
        filename_ = str(datetime.datetime.now()).replace(' ', '_') + \
            werkzeug.secure_filename(imagefile.filename)
        filename = os.path.join(UPLOAD_FOLDER, filename_)
        imagefile.save(filename)
        logging.info('Saving to %s.', filename)
        input_image = caffe.io.load_image(filename)

    except Exception as err:
        logging.info('Uploaded image open error: %s', err)
        return err
    result = app.clf.classify_image(input_image)
    return json.dumps(dict(result[1]))


def start_tornado(app, port=5000):
    http_server = tornado.httpserver.HTTPServer(
        tornado.wsgi.WSGIContainer(app))
    http_server.listen(port)
    print("Tornado server starting on port {}".format(port))
    tornado.ioloop.IOLoop.instance().start()


def start_from_terminal(app):
    """
    Parse command line options and start the server.
    """
    parser = optparse.OptionParser()
    parser.add_option(
        '-d', '--debug',
        help="enable debug mode",
        action="store_true", default=False)
    parser.add_option(
        '-p', '--port',
        help="which port to serve content on",
        type='int', default=5000)
    parser.add_option(
        '-g', '--gpu',
        help="use gpu mode",
        action='store_true', default=False)

    opts, args = parser.parse_args()
    inc.ImagenetClassifier.default_args.update( { 'gpu_mode': opts.gpu})

    # Initialize classifier + warm start by forward for allocation
    app.clf = inc.ImagenetClassifier(**inc.ImagenetClassifier.default_args)
    app.clf.net.forward()

    if opts.debug:
        app.run(debug=True, host='0.0.0.0', port=opts.port)
    else:
        start_tornado(app, opts.port)


if __name__ == '__main__':
    logging.getLogger().setLevel(logging.INFO)
    if not os.path.exists(UPLOAD_FOLDER):
        os.makedirs(UPLOAD_FOLDER)
    start_from_terminal(app)
