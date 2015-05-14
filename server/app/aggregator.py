__author__ = 'torrytufteland'

import os.path as osp
import os
import csv
import pdb
import glob
import caffe
import logging
import numpy as np
import imagenetclassifier as inc
from os.path import expanduser


class Classifier:

    def __init__(self, work_dir=expanduser("~") + '/Dropbox/DeepSmartGlasses/scenes'):
        inc.ImagenetClassifier.default_args.update({'gpu_mode': True})

        # Initialize classifier + warm start by forward for allocation
        self.clf = inc.ImagenetClassifier(**inc.ImagenetClassifier.default_args)
        self.clf.net.forward()
        self.work_dir = work_dir

    def compute_average_score_by_scenes(self):
        scene_dirs = glob.glob(self.work_dir+'/*')
        all_scenes = []
        for scene in scene_dirs:

            image_list = glob.glob(scene+'/*.jpg')

            results = []
            for filename in image_list:
                meta = self.classify_image(filename)
                ground_truth = meta[1]
                results.append([score for score in meta[0] if score[0] == ground_truth][0][1])

            if len(results) != 0:
                avg_for_scene = sum(results) / len(results)
                print(osp.basename(scene) + ": " + str(avg_for_scene))
                all_scenes.append((osp.basename(scene), avg_for_scene))
            else:
                print 'Error in scene: ' + scene

        content = sorted(all_scenes, key=lambda tup: tup[1], reverse=True)
        column_names = ["Scene", "Score"]

        self.write_to_csv(self.work_dir + '/average_score_by_scene.csv', content, column_names)

    def get_files_in_subfolders(self, directory, extension="jpg"):
        folders = [x[0] for x in os.walk(directory)]
        files = []
        for f in folders:
            files.extend(glob.glob("{}/*.{}".format(f, extension)))
        return files

    def compute_all_ranks(self):
        files = self.get_files_in_subfolders(self.work_dir)
        scenes = [osp.basename(x[0]) for x in os.walk(self.work_dir)][1:]
        categories = list(set([osp.basename(osp.splitext(f)[0]) for f in files]))

        index_results = np.zeros((len(scenes), len(categories)))
        index_results.fill(-1)
        score_results = np.zeros((len(scenes), len(categories)))
        score_results.fill(-1)

        for i, scene in enumerate(scenes):
            for j, cat in enumerate(categories):
                image = os.path.join(self.work_dir, scene, cat + ".jpg")
                if osp.isfile(image):
                    meta, ground_truth = self.classify_image(image)
                    for idx, val in enumerate(meta):
                        if val[0] == ground_truth:
                            print("Score at {}/{} is {}".format(scene, cat, val[1]))
                            index_results[i][j] = idx + 1
                            score_results[i][j] = val[1]
        cat_mean = (index_results * (index_results != -1)).mean(axis=1)
                
        outfile = osp.join(self.work_dir, '_index_ranks.csv')
        open(outfile, 'w').close()
        categories.insert(0, '')
        categories.append('Average pr. scene')

        with open(outfile, 'w') as fp:
            a = csv.writer(fp, delimiter=';')
            a.writerow(categories)

            for i in range(0, index_results.shape[0]):
                row = [str(int(item)) for item in index_results[i].tolist()]
                row.insert(0, scenes[i])
                mean_string = "{:.2f}".format(cat_mean[i])
                row.append(mean_string.replace(".", ","))
                a.writerow(row)

    def compute_average_rank_by_scenes(self):
        scene_dirs = [x[0] for x in os.walk(self.work_dir)]
        all_scenes = []
        for scene in scene_dirs:
            print("\nClassifying scene '{}'".format(osp.basename(scene)))
            image_list = glob.glob(scene+'/*.jpg')
            results = []
            for filename in image_list:
                meta = self.classify_image(filename)
                ground_truth = meta[1]
                ranks = []
                for idx, val in enumerate(meta[0]):
                    if val[0] == ground_truth:
                        ranks.append(idx)
                results.extend(ranks)

            if len(results) != 0:
                avg_for_scene = sum(results) / len(results)
                all_scenes.append((osp.basename(scene), avg_for_scene))
            else:
                print 'Error in scene: ' + scene

        content = sorted(all_scenes, key=lambda tup: tup[1])
        column_names = ["Scene", "Average rank"]

        outfile = osp.join(self.work_dir, 'average_rank_by_scene.csv')
        self.write_to_csv(outfile, all_scenes, column_names)


    def write_to_csv(self, path, content, column_names, order_by_column_number=1):
        outfile = path
        print('\nSaving results...')

        # Clear previous results
        open(outfile, 'w').close()

        rows = sorted(content, key=lambda tup: tup[order_by_column_number])
        # Write results to results.csv in WORK_DIR
        with open(outfile, 'w') as fp:
            a = csv.writer(fp, delimiter=';')
            a.writerow(column_names)
            a.writerows(rows)
        print("Saved results to '{}'".format(path))
        return True

    def classify_image(self, filename):
        try:
            input_image = caffe.io.load_image(filename)
        except Exception as err:
            logging.info('Load image error: %s', err)
        print('\nClassifying image: ' + filename)

        scores = self.clf.net.predict([input_image], oversample=True).flatten()
        indices = (-scores).argsort()
        predictions = self.clf.labels[indices]

        meta = [
            (p, scores[i])
            for i, p in zip(indices, predictions)
        ]
        ground_truth = osp.basename(osp.splitext(filename)[0])
        return meta, ground_truth

def run():
    classifier = Classifier()
    # classifier.compute_average_rank_by_scenes()
    classifier.compute_all_ranks()

def play():
    classifier = Classifier()
    # classifier.compute_average_score_by_scenes()
    result = classifier.classify_image("/Users/torrytufteland/Pictures/BT200/wall clock.jpg")
    ground_truth = "wall clock"
    ranks = []
    # print(result[0])
    for idx, val in enumerate(result[0]):
        if val[0] == ground_truth:
            ranks.append((val[0], idx))
    print ranks

run()