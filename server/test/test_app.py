import unittest
import numpy as np
from PIL import Image
import cStringIO as StringIO
import app.exifutil as ex


def create_test_image(w = 512, h = 512):
    img = np.zeros( (w,h,3), dtype=np.uint8)
    img.fill(1.1)
    return img
        

class TestEmbedImageHtml(unittest.TestCase):

    def test_imageresize(self):
        img = create_test_image()
        
        img_pil = Image.fromarray(img, 'RGB')
        img_pil = img_pil.resize((256,256))

        self.assertEqual(img_pil.size, ((256,256)))

    def test_imagearraytostring(self):
        img = create_test_image(4,4)
        string_buf = StringIO.StringIO()
        
        img_pil = Image.fromarray(img, 'RGB')
        img_pil.save(string_buf, format='png')
        result = string_buf.getvalue()
        self.assertTrue(result, basestring)


class TestExifUtil(unittest.TestCase):

    def test_filejpgtoimage(self):
        img = Image.open('test/test.jpg')
        self.assertIsInstance(img, Image.Image)

    def test_imagenotnone(self):
        img = Image.open('test/test.jpg')
        self.assertNotEqual(img, None)

    def test_imagetoarray(self):
        img = Image.open('test/test.jpg')

        img = np.asarray(img)
        self.assertIsInstance(img, np.ndarray)

    def test_imagedimensions(self):
        img = Image.open('test/test.jpg')

        img = np.asarray(img)
        self.assertEqual(img.ndim, 3 or 2)

    def test_arrayisuint8(self):
        img = Image.open('test/test.jpg')
        img = np.asarray(img)

    def test_openorientedimreturnsarray(self):
        img = ex.open_oriented_im('test/test.jpg')
        self.assertIsInstance(img, np.ndarray)

        
if __name__ == '__main__':
    unittest.main()