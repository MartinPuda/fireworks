; Created 31. 12. 2020
; Click on screen to add fireworks.

(ns fireworks.core
  (:require [quil.core :as q]
            [quil.middleware :as m]))

(defn draw-particle [particle]
    (let [{x :x y :y _ :angle} particle]
      (q/fill 255 ;(* 255 (Math/random))
              (* 255 (Math/random))
              0)
     (q/ellipse x y 5 5)))

(defn create-particle [x y angle]
  {:x x
   :y y
   :timer (+ 15 (Math/round (* 15 (Math/random))))
   :angle angle
   })

(defn create-bomb [x y]
  {
   :x x
   :y y
   :timer (+ 25 (Math/round (* 50 (Math/random))))
   }
  )

(defn create-particles [x y num]
    (if (= 360 num) '()
                    (cons (create-particle x y num)
                          (create-particles x y (+ num 1)))))

(defn setup []
  (q/frame-rate 30)
  (q/color-mode :rgb)
  (q/rect-mode :center)
  {:bombs '()
   :particles '()
   :time 0})

(defn update-particles-help [bombs]
  (if (empty? bombs) '()
                     (let [bomb (first bombs)]
                       (concat
                       (if (= 1 (:timer bomb))
                         (create-particles (:x bomb)
                                           (:y bomb)
                                           0) '())
                       (update-particles-help (rest bombs))))))

(defn update-particles [state]
  (concat (:particles state) (update-particles-help (:bombs state))))

(defn move-particles [particles]
  (map (fn [pt] (let [ang (:angle pt)]
                                          {:x (+ (:x pt)
                                                 (* 10 (Math/cos
                                                        (Math/toRadians ang))))
                                           :y (+ (:y pt)
                                                 (* 10 (Math/sin
                                                        (Math/toRadians ang))))
                                           :timer (:timer pt)
                                           :angle ang})) particles))

(defn move-bombs [bombs]
  (map (fn [bomb] (update bomb :y - 5)) bombs))

(defn dec-timers [bombs]
  (map (fn [bomb] (update bomb :timer - 1)) bombs))

(defn dec-particle-timers [particles]
  (map (fn [particle] (update particle :timer - 1)) particles))

(defn dead? [obj]
  (= (:timer obj) 0))

(defn remove-dead [obj]
  (remove dead? obj))

(defn draw-bomb [bomb]
  (let [{x :x y :y} bomb]
    (q/fill 255 0 0)
    (q/triangle x y
                (- x 20) (+ y 20)
                (+ x 20) (+ y 20))
    (q/rect x (+ y 45) 25 50)
    (q/fill 250 75 0)
    (q/rect x (+ y 105) 5 70)
    (q/fill 0 0 0)))

(defn add-bomb [state mouse]
  {:bombs     (cons (create-bomb
                      (q/mouse-x)
                      (q/mouse-y))
                    (:bombs state))
   :time      (+ (:time state) 1)
   :particles (:particles state)})

(defn update-state [state]
  {:bombs     (-> (:bombs state)
                  (move-bombs)
                  (dec-timers)
                  (remove-dead))
   :time      (+ (:time state) 1)
   :particles (remove-dead
                (dec-particle-timers
               (move-particles (update-particles state))))
   })

(defn draw-state [state]
  (q/background 0)
  (q/fill 0)
      (doseq [bomb (:bombs state)]
        (draw-bomb bomb))
      (doseq [particle (:particles state)]
        (draw-particle particle)))


(q/defsketch fireworks
  :title "Fireworks"
  :size [500 500]
  ; setup function called only once, during sketch initialization.
  :setup setup
  ; update-state is called on each iteration before draw-state.
  :update update-state
  :draw draw-state
   :mouse-released add-bomb
  :features [:keep-on-top]
  ; This sketch uses functional-mode middleware.
  ; Check quil wiki for more info about middlewares and particularly
  ; fun-mode.
  :middleware [m/fun-mode])