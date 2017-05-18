(ns lytek.chron.elements
  (:require [lytek.macros :as lymac]))

(lymac/defrecord-with-default
  Merit
  ""
  {:name "Default Merit"
   :possible-ranks #{1 2 3 4 5}
   :prereq [:and [:melee 3] [:brawl 2]]
   :static-tags [[:blorp]
                 []
                 [:blorp]]
   :description "This is the default merit. If you're seeing this text, it means that something went a bit wrong."})