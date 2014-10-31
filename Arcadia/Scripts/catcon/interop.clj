(ns catcon.interop)

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

;; for repl redefs, we need something way better than this tho
;; dataflow stuff would be nice
(defmacro defscn [name & body]
  `(def ~name 
     (do (declare ~name)
         (when-not
             (instance? clojure.lang.Var+Unbound
               (var-get (resolve (quote ~name))))
           (destroy ~name))
         ~@body)))

