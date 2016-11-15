(ns lytek.col)

(defn add-up-numbah-map [numba-map]
  (reduce-kv (fn [r k v] (+ r v)) 0 numba-map))

(defn add-map-of-numbah-maps [map-of-numbah-maps]
  (into {} (map
             (fn [[k v]]
               [k (add-up-numbah-map v)])
             map-of-numbah-maps)))

(defn sort-numbah-map [numbah-map]
  (sort (fn [[k1 v1 :as t1] [k2 v2 :as t2]] (- v1 v2)) numbah-map))

(defn scrub-nulls-from-vector [vector-to-scrub]
  (reduce (fn [vector-being-built thing]
            (if (nil? thing)
              vector-being-built
              (into vector-being-built [thing])))
          []
          vector-to-scrub))

(defn filter-with-not [pred coll]
  [(filter pred coll)
   (filter #(not (pred %)) coll)])

(defn namemap [seq-of-named]
  (into {} (map (fn [thing] [(:name thing) thing]) seq-of-named)))

(defn indices [pred coll]
  (keep-indexed #(when (pred %2) %1) coll))

(defn first-index [pred coll]
  (first (indices pred coll)))