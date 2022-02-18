#!/usr/bin/env bb

(ns tasks
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(def version-file (io/file "resources" "LIB_DEPRECATIONS_VERSION"))

(defn version []
  (str/trim (slurp version-file)))

(defn bump-version []
  (let [version-string (version)
        [major minor patch] (str/split version-string #"\.")
        new-version (str/join "." [major minor (inc  (Integer/parseInt patch))])]
    (spit version-file new-version)
    new-version))
