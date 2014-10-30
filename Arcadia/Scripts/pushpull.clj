(ns pushpull
  (:use arcadia.core)
  (:import [UnityEngine
            Vector3
            Time
            Gizmos
            Color
            Debug
            Plane
            Mathf]))

(defmacro v [x y z]
  `(Vector3. ~x ~y ~z))

(def mesh #{[(v 0 1 0)
             (v 1 2 -1)
             (v 1 2 2)
             (v 0 1 1)]
                       
           [(v 0 0 0)
            (v 1 0 -1)
            (v 1 0 2)
            (v 0 0 1)]
                             
           [(v 1 0 -1)
            (v 1 2 -1)
            (v 1 2 2)
            (v 1 0 2)]
                       
           [(v 0 0 0)
            (v 0 1 0)
            (v 0 1 1)
            (v 0 0 1)]
                       
           [(v 1 2 2)
            (v 0 1 1)
            (v 0 0 1)
            (v 1 0 2)]
                       
           [(v 0 0 0)
            (v 0 1 0)
            (v 1 2 -1)
            (v 1 0 -1)]})

(defn edges [face]
  (->> face
       cycle
       (take (inc (count face)))
       (partition 2 1)
       (map set)
       set))

(defn vertices [face]
  (set face))

(defn normal [[a b c]]
  (.normalized (vx (v- a b) (v- b c))))

(defn v+ [^Vector3 a ^Vector3 b] (Vector3/op_Addition a b))
(defn v- [^Vector3 a ^Vector3 b] (Vector3/op_Subtraction a b))
(defn v* [a b] (Vector3/op_Multiply a b))
(defn v÷ [a b] (Vector3/op_Division a b))
(defn vx [^Vector3 a ^Vector3 b] (Vector3/Cross a b))

(defn centroid [vs]
  (v÷ (reduce v+ vs)
      (count vs)))

(defn gizmo-color [^Color c]
  (set! Gizmos/color c))

(defn gizmo-arrow [^Vector3 from ^Vector3 to]
  (Gizmos/DrawLine from to)
  (Gizmos/DrawRay to (v* 0.5 (Vector3/RotateTowards
                                (v- to from)
                                (Vector3/Cross to (v- to from))
                                -2.75 0)))
  (comment (Gizmos/DrawRay to
                  (v* 0.5 Vector3/left))))

(defn gizmo-line [^Vector3 from ^Vector3 to]
  (Gizmos/DrawLine from to))

(defn gizmo-ray [^Vector3 from ^Vector3 dir]
  (Gizmos/DrawRay from dir))

(defn gizmo-point [^Vector3 v]
  (Gizmos/DrawSphere v 0.075))

(defn face->plane [f]
  (let [n (normal f)]
    {:normal n 
     :distance (Vector3/Dot n (centroid f))}))

(defn gizmo-face-plane [f]
  (let [c (centroid f)
        n (normal f)
        pv1 (.normalized (apply v- (take 2 f)))
        pv2 (.normalized (Vector3/Cross n pv1))]
        
    (gizmo-ray c n)
    (doseq [x (range -10 10)
            y (range -10 10)]
      (let [c (reduce v+ [c
                          (v* pv1 x)
                          (v* pv2 y)])]
        (gizmo-ray c pv1)
        (gizmo-ray c pv2)
        (gizmo-ray c (v* -1 pv1))
        (gizmo-ray c (v* -1 pv2))))))

(defn gizmo-plane [p]
  (let [n (.normalized (p :normal))
        c (v* (p :distance) n)
        crossh (.normalized 
                (Vector3/Cross n (Vector3/Cross Vector3/right n)))
        crossv (.normalized 
                (Vector3/Cross n (Vector3/Cross Vector3/up n)))]
    (gizmo-ray c n)
    (doseq [x (range -10 10)
            y (range -10 10)]
      (let [c (reduce v+ [c
                          (v* crossh x)
                          (v* crossv y)])]
        (gizmo-ray c crossh)
        (gizmo-ray c crossv)
        (gizmo-ray c (v* -1 crossh))
        (gizmo-ray c (v* -1 crossv))  ))))

(defn gizmo-face [f]
  (let [verts f
        closed-verts (conj verts (first verts))
        edges (partition 2 1 closed-verts)]
        
    ;; draw sides
    (doseq [e edges]
      (apply gizmo-line e))
    
    ;; draw normal
    (let [c (centroid f)]
      (gizmo-arrow c
                  (v+ c
                      (v* 0.2 (normal f)))))))

(defn gizmo-mesh [m]
  (doseq [f m]
    (gizmo-face f)))

(defn intersect-2-planes [{d1 :distance
                           n1 :normal
                           :keys [distance normal]}
                          {d2 :distance
                           n2 :normal
                           :keys [distance normal]}]
  (let [n1 (.normalized n1)
        n2 (.normalized n2)
        pos1 (v* d1 n1)
        pos2 (v* d2 n2)
        line-vec (Vector3/Cross n1 n2)
        ldir (Vector3/Cross n2 line-vec)
        den (Vector3/Dot n1 ldir)
        p1-to-p2 (v- pos1 pos2)
        t (/ (Vector3/Dot n1 p1-to-p2) den)
        line-point (v+ pos2 (v* t ldir))]
    {:point line-point
     :direction line-vec}))

(defn intersect-line-plane [{:keys [point direction]}
                            {:keys [distance normal]}]
  (let [direction (.normalized direction)
        normal (.normalized normal)
        plane-point (v* distance normal)
        
        dot-num (Vector3/Dot (v- plane-point point) normal)
        dot-den (Vector3/Dot direction normal)
        len (/ dot-num dot-den)
        v (v* direction len)]
    (v+ point v)))

(defn intersect-3-planes [p1 p2 p3]
  (let [l (intersect-2-planes p1 p2)]
    (intersect-line-plane l p3)))

(defn adjacent-faces [mesh face v]
  (filter #(and ((vertices %) v)
                (not= % face))
          mesh))

(defn pull-vertex [mesh face target v]
  (let [adj-faces (adjacent-faces mesh face v)]
    (apply intersect-3-planes target
           (map face->plane adj-faces))))

(defn pull [mesh face target]
  (-> mesh
      (disj face)
      (conj (map #(pull-vertex mesh face target %) face))
      (->> (map (fn [f]
                  (mapv #(if ((vertices face) %)
                          (pull-vertex mesh face target %)
                          %) f)))
           set)))

(defn angle [{n1 :normal
              :keys [normal]}
             {n2 :normal
              :keys [normal]}]
  (Mathf/Acos (/ (Vector3/Dot n1 n2)
                 (* (.magnitude n1)
                    (.magnitude n2)))))

(defn insert-faces [mesh face target d theta]
  (doseq [e (edges face)]
    (let [f-adj (->> mesh
                     (filter #(and ((edges %) e)
                                   (not= % face)))
                     first)]
      (if (or (nil? f-adj)
              (< (angle target (face->plane f-adj))
                 (- (/ Mathf/PI 2) theta)))
        (->> e cycle (take 4) vec)) ;; new face
      
    ) ) )

(do
  (defn gizmos []
    (let [p {:normal (Vector3. 1 0 0)
             :distance 2}
          face (nth (vec mesh) 2)]
          
      (gizmo-color Color/yellow)
      ; (gizmo-plane p)
      
      (doseq [v face]
        (gizmo-color Color/white)
        (gizmo-point v)
        (let [vfaces (filter #(and ((vertices %) v)
                                   (not= % face))
                             mesh)
              v* (apply intersect-3-planes p (map face->plane vfaces))]
          (gizmo-color Color/red)
          (gizmo-point v*)
          (gizmo-line v v*)))
      
      (gizmo-color Color/red)
      (gizmo-mesh (pull mesh face p))
      
      (gizmo-color Color/white)
      (gizmo-mesh mesh)
        
      ))
  (UnityEditor.SceneView/RepaintAll))