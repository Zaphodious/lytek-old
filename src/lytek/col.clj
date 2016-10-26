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