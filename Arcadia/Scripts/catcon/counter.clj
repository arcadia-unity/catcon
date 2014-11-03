(ns catcon.counter
  (:use arcadia.core)
  (:import [UnityEngine Debug]))

(defcomponent Counter []
  (Update [this]
    (set! (.. this guiText text)
          (str "Cats: "
               (count (objects-typed catcon.cat.Cat))))))