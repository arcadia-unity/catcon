(ns hooks
  (:use arcadia.core))

(defmacro invoke-if-bound [sym]
  `(if-let [~sym ((ns-map '~'user) '~sym)]
    (~sym)))

; via jplur_ ;)
(defcomponent Hooks []
  (Start [this]
    (ns user)
    (require 'user)
    (invoke-if-bound setup))
  
  (LateUpdate [this]
    (invoke-if-bound late-update))
  
  (FixedUpdate [this]
    (invoke-if-bound fixed-update))
  
  (Update [this]
    (invoke-if-bound update))
  
  (OnGUI [this]
    (invoke-if-bound gui))
  
  (OnDrawGizmos [this]
    (invoke-if-bound gizmos))
  
  (OnDrawGizmosSelected [this]
    (invoke-if-bound gizmos-selected)))