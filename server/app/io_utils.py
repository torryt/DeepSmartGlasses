__author__ = 'torrytufteland'

import os
import os.path as osp
import re
import glob
from os.path import expanduser

WORK_DIR = expanduser("~") + '/scenes/'
all_scenes = glob.glob(WORK_DIR+'/*')


def fix_digital_watch():
    for scene in all_scenes:
        image_list = glob.glob(scene+'/digwatch.jpg')
        for filename in image_list:
            os.rename(filename, scene+'/digital watch.jpg')


def fix_names():
    for scene in all_scenes:
        image_list = glob.glob(scene+'/*.jpg')
        for filename in image_list:
            regex = re.search('[^_]*', osp.basename(filename))
            newfile = scene + '/' +regex.group() + '.jpg'
            os.rename(filename, newfile)