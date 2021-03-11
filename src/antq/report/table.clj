(ns antq.report.table
  (:require
   [antq.diff :as diff]
   [antq.report :as report]
   [antq.util.dep :as u.dep]
   [antq.util.ver :as u.ver]
   [clojure.pprint :as pprint]
   [clojure.set :as set]))

(defn skip-duplicated-file-name
  [sorted-deps]
  (loop [[dep & rest-deps] sorted-deps
         last-file nil
         result []]
    (if-not dep
      result
      (if (= last-file (:file dep))
        (recur rest-deps last-file (conj result (assoc dep :file "")))
        (recur rest-deps (:file dep) (conj result dep))))))

(defmethod report/reporter "table"
  [deps _options]
  ;; Show table
  (if (seq deps)
    (->> deps
         (sort u.dep/compare-deps)
         skip-duplicated-file-name
         (map #(assoc % :latest-version (u.ver/normalize-latest-version %)))
         (map #(set/rename-keys % {:version :current
                                   :latest-version :latest}))
         (pprint/print-table [:file :name :current :latest]))
    (println "All dependencies are up-to-date."))

  ;; Show diff URLs
  (let [urls (->> deps
                  (sort u.dep/compare-deps)
                  (keep diff/get-diff-url)
                  (distinct))]
    (when (seq urls)
      (println "\nAvailable diffs:")
      (doseq [u urls]
        (println "-" u)))))
