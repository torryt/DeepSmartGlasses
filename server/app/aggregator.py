__author__ = 'torrytufteland'

import os.path as osp
import glob
import caffe
import logging
import numpy as np
import imagenetclassifier as inc
from os.path import expanduser

WORK_DIR = expanduser("~") + '/Dropbox/DeepSmartGlasses/scenes'

inc.ImagenetClassifier.default_args.update({'gpu_mode': True})

# Initialize classifier + warm start by forward for allocation
clf = inc.ImagenetClassifier(**inc.ImagenetClassifier.default_args)
clf.net.forward()

scene_dirs = glob.glob(WORK_DIR+'/*')
all_scenes = []

for scene in scene_dirs:

    image_list = glob.glob(scene+'/*.jpg')

    results = []
    for filename in image_list:
        try:
            input_image = caffe.io.load_image(filename)

        except Exception as err:
            logging.info('Load image error: %s', err)

        print('Classifying image: ' + filename)

        scores = clf.net.predict([input_image], oversample=True).flatten()

        indices = (-scores).argsort()
        predictions = clf.labels[indices]

        meta = [
            (p, scores[i])
            for i, p in zip(indices, predictions)
        ]

        ground_truth = osp.basename(osp.splitext(filename)[0])

        results.append([item for item in meta if item[0] == ground_truth][0][1])

    if len(results) != 0:
        avg_for_scene = sum(results) / len(results)
        print(osp.basename(scene) + ": " + str(avg_for_scene))
        all_scenes.append((osp.basename(scene), avg_for_scene))
    else:
        print 'Error in scene: ' + scene


outfile = WORK_DIR + '/results.csv'
print('Saving results to ' + outfile + '\n...')

# Clear previous results
open(outfile, 'w').close()

sorted_scenes = sorted(all_scenes, key=lambda tup: tup[1], reverse=True)
# Write results to results.csv in WORK_DIR
import csv
with open(outfile, 'w') as fp:
    a = csv.writer(fp, delimiter=';')
    a.writerow(["Scene", "Score"])
    a.writerows(sorted_scenes)