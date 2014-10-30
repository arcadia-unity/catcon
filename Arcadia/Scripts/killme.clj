(ns killme
  (:use arcadia.core)
  (:import [UnityEngine Debug]))

(defcomponent killme []
  ; use this for initialization
  (Start [this]
    (destroy* (.gameObject this)))
  
  ; update is called once per frame
  (Update [this]))