(ns catcon.interop
  (:import [UnityEngine Application Vector3 Physics Ray RaycastHit Mathf Input]
           PhysicsShim))

(defn get-components
  ([^UnityEngine.GameObject go]
   (.GetComponents go UnityEngine.Component))
  
  ([^UnityEngine.GameObject go ^System.Type t]
   (.GetComponents go t)))

(defmacro set-with! [obj [sym prop] & body]
  `(let [obj# ~obj
         ~sym (. obj# ~prop)]
     (set! (. obj# ~prop) (do ~@body))))

(defmacro condcast-> [expr xsym & clauses]
  (let [exprsym (gensym "exprsym_")
        [clauses default] (if (even? (count clauses))
                            [clauses nil] 
                            [(butlast clauses)
                             [:else
                              `(let [~xsym ~exprsym]
                                 ~(last clauses))]])
        cs (->> clauses
             (partition 2)
             (mapcat
               (fn [[t then]]
                 `[(instance? ~t ~exprsym)
                   (let [~(with-meta xsym {:tag t}) ~exprsym]
                     ~then)])))]
    `(let [~exprsym ~expr]
       ~(cons 'cond
          (concat cs default)))))

(defn restart []
  (Application/LoadLevel Application/loadedLevelName))

(defmacro aseq [ary]
  (let [^type t (-> ary resolve deref (aget 0) type)]
    `(new ~(symbol (str "clojure.lang.TypedArraySeq`1[" t "]"))
          nil
          ~ary
          0)))

(defn raycast [^Ray ray]
  (PhysicsShim/Raycast ray))

(defn mouse?
  ([] (mouse? 0))
  ([^long button] (Input/GetMouseButtonDown button)))