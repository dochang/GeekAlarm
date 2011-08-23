(ns com.geekalarm.server.mathml-utils
  (:require [clojure.contrib.math :as math]))

; cljml - structure, representing math ml xml

(declare matrix-to-cljml)
(declare ratio-to-cljml)

(defn cljml
  ([name children] (reduce conj [name] children))
  ([value] (cond (ratio? value) (ratio-to-cljml value)
		 (number? value) [:mn (math/round value)]
		 (string? value) [:mi value]
		 (coll? value) (matrix-to-cljml value)
		 :else [:mi (str value)])))

(defn ratio-to-cljml [ratio]
  [:mfrac
   [:mn (numerator ratio)]
   [:mn (denominator ratio)]])


(defn matrix-to-cljml [matrix]
  (let [get-row (fn [row]
		  (cljml :mtr
			 (map (fn [val] [:mtd (cljml val)]) row)))]
    (cljml :mtable (map get-row matrix))))

(defn cljml-to-str [cljml]
  (if (empty? cljml)
    ""
    (let [node-name (name (first cljml))
          children (rest cljml)
          child-to-str (fn [child]
                         (cond (keyword? child) (format "<%s/>" (name child))
                               (vector? child) (cljml-to-str child)
                               :else (str child)))]
      (->> (map child-to-str children)
           (apply str)
           (format "<%1$s>%2$s</%1$s>" node-name)))))
