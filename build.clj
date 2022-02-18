;; see https://ask.clojure.org/index.php/10905/control-transient-deps-that-compiled-assembled-into-uberjar?show=10913#c10913
(require 'clojure.tools.deps.alpha.util.s3-transporter)

(ns build
  (:refer-clojure :exclude [compile])
  (:require
   [clojure.string :as str]
   [clojure.tools.build.api :as b]
   [deps-deploy.deps-deploy :as dd]))

(def lib 'com.github.clj-easy/lib-deprecations)
(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))
(def with-svm-basis (b/create-basis {:project "deps.edn"
                                     :aliases [:svm]}))

(def version-file "resources/LIB_DEPRECATIONS_VERSION")
(def version (str/trim (slurp version-file )))
(def jar-file (format "target/%s-%s.jar" (name lib) version))

(defn clean [_]
  (b/delete {:path "target"}))

(defn jar [_]
  (println "Producing jar:" jar-file)
  (b/write-pom {:class-dir class-dir
                :lib lib
                :version version
                :basis basis
                :src-dirs ["src"]})
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  (b/jar {:class-dir class-dir
          :jar-file jar-file})
  (println "Done building jar.")
  (println "Jar is up to date."))

(defn install
  [_]
  (jar {})
  (b/install {:basis basis
              :lib lib
              :version version
              :jar-file jar-file
              :class-dir class-dir})
  (println "Installed" lib version "in local maven repo."))

(defn deploy [opts]
  (println "All set for deployment 🚀🚀")
  (jar {})
  (println "Deploying version" jar-file "to Clojars.")
  (dd/deploy (merge {:installer :remote
                     :artifact jar-file
                     :pom-file (b/pom-path {:lib lib :class-dir class-dir})}
                    opts))
  opts)
