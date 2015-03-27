import os
import caffe
import flask
import logging
import datetime
from flask import Flask, request, redirect, url_for
from werkzeug.utils import secure_filename
from os.path import expanduser


REPO_DIRNAME = os.path.abspath(expanduser("~") + '/caffe')
UPLOAD_FOLDER = '/tmp/deepsmartg/uploads'
ALLOWED_EXTENSIONS = set(['txt', 'pdf', 'png', 'jpg', 'jpeg', 'gif'])

app = Flask(__name__)
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER


@app.route('/')
def index():
    return flask.render_template('index.html', has_result=False)

@app.route('/classify_upload', methods=['POST'])
def classify_upload():
    try:
        imagefile = flask.request.files['imagefile']
        filename_ = str(datetime.datetime.now()).replace(' ', '_') + \
            secure_filename(imagefile.filename)
        filename = os.path.join(UPLOAD_FOLDER, filename_)
        imagefile.save(filename)
        logging.info('Saving to %s.', filename)
        #image = exifutil.open_oriented_im(filename)
    except Exception as err:
        logging.info('Uploaded image open error %s', err)
        return 'Cannot open uploaded image'

    if request.method == 'POST':
        file = request.files['file']
        if file and allowed_file(file.filename):
            filename = secure_filename(file.filename)
            file.save(os.path.join(app.config['UPLOAD_FOLDER'], filename))
            return redirect(url_for('uploaded_file',
                                    filename=filename))
    return 'Could not upload file'


def allowed_file(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1] in ALLOWED_EXTENSIONS