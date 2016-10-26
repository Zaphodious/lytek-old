(ns lytek.character-creation-validation-spec
  (:require [speclj.core :refer :all]
            [lytek.core :refer :all]
            [lytek.character :as lychar]
            [lytek.character.elements :as lyelm]
            [lytek.validation.creation :as lycreate]
            [lytek.chron.merit :as lycmerit]
            ))

(describe "the test"
          (it "FIXME, I fail."
              (should= 1 1)))



(describe "Validating Attributes During Creation"
          (it "properly determines when attributes have been fully purchased without bonus points"
              (should= {:bonus-points-spent 0, :attribute-points-remaining {:mental 0, :social 0, :physical 0}}
                       (lycreate/attribute-info
                         {::lyelm/chartype :solar
                          ::lyelm/attribute-ranks
                                           {:strength   5 :dexterity 5 :stamina 1
                                            :appearance 3 :charisma 3 :manipulation 3
                                            :wits       3 :intelligence 3 :perception 1}})))
          (it "knows when there are points remaining"
              (should= {:bonus-points-spent 0, :attribute-points-remaining {:mental 1, :social 2, :physical 3}}
                       (lycreate/attribute-info
                         {::lyelm/chartype :solar
                          ::lyelm/attribute-ranks
                                           {:strength   2 :dexterity 5 :stamina 1
                                            :appearance 1 :charisma 3 :manipulation 3
                                            :wits       2 :intelligence 3 :perception 1}})))
          (it "knows when attributes have used bonus points"
              (should= {:bonus-points-spent 23, :attribute-points-remaining {:mental 0, :social 0, :physical 0}}
                       (lycreate/attribute-info
                         {::lyelm/chartype :solar
                          ::lyelm/attribute-ranks
                                           {:strength   5 :dexterity 5 :stamina 4
                                            :appearance 4 :charisma 4 :manipulation 3
                                            :wits       4 :intelligence 3 :perception 1}}))))

(describe "Validating Abilities During Creation"
          (it "knows when abilities have been purchased with no bonus points"
              (should= {:bonus-points-spent 0, :ability-points-remaining 0}
                       (lycreate/ability-info
                         {::lyelm/chartype :solar
                          ::lyelm/ability-ranks
                                           {:lore        3, :melee 3, :investigation 3, :dodge 3, :presence 3,
                                            :performance 3, :linguistics 3, :war 3, :socialize 3, :survival 1,
                                            :bureaucracy 0, :occult 0, :resistance 0, :brawl 0, :ride 0,
                                            :athletics   0, :thrown 0, :archery 0, :integrity 0, :awareness 0,
                                            :larceny     0, :sail 0, :medicine 0, :stealth 0}})))
          (it "knows that any rank over 3 involves bonus points"
              (should= {:bonus-points-spent 4, :ability-points-remaining 0}
                       (lycreate/ability-info
                         {::lyelm/chartype          :solar
                          ::lyelm/favored-abilities lyelm/abilities
                          ::lyelm/caste-abiliites   lyelm/abilities
                          ::lyelm/ability-ranks
                                                    {:lore        5, :melee 3, :investigation 3, :dodge 5, :presence 3,
                                                     :performance 3, :linguistics 3, :war 3, :socialize 3, :survival 1,
                                                     :bureaucracy 0, :occult 0, :resistance 0, :brawl 0, :ride 0,
                                                     :athletics   0, :thrown 0, :archery 0, :integrity 0, :awareness 0,
                                                     :larceny     0, :sail 0, :medicine 0, :stealth 0}})))
          (it "knows when there are points left to spend"
              (should= {:bonus-points-spent 0, :ability-points-remaining 3}
                       (lycreate/ability-info
                         {::lyelm/chartype :solar
                          ::lyelm/ability-ranks
                                           {:lore        2, :melee 2, :investigation 2, :dodge 3, :presence 3,
                                            :performance 3, :linguistics 3, :war 3, :socialize 3, :survival 1,
                                            :bureaucracy 0, :occult 0, :resistance 0, :brawl 0, :ride 0,
                                            :athletics   0, :thrown 0, :archery 0, :integrity 0, :awareness 0,
                                            :larceny     0, :sail 0, :medicine 0, :stealth 0}})))
          (it "knows when bonus points have been spent while also having spent bonus points"
              (should= {:bonus-points-spent 4, :ability-points-remaining 3}
                       (lycreate/ability-info
                         {::lyelm/chartype          :solar
                          ::lyelm/favored-abilities lyelm/abilities
                          ::lyelm/caste-abiliites   lyelm/abilities
                          ::lyelm/ability-ranks
                                                    {:lore        2, :melee 2, :investigation 2, :dodge 5, :presence 5,
                                                     :performance 3, :linguistics 3, :war 3, :socialize 3, :survival 1,
                                                     :bureaucracy 0, :occult 0, :resistance 0, :brawl 0, :ride 0,
                                                     :athletics   0, :thrown 0, :archery 0, :integrity 0, :awareness 0,
                                                     :larceny     0, :sail 0, :medicine 0, :stealth 0}})))
          (it "knows that, even when no attributes are over 3, more then 28 points total involves bonus points"
              (should= {:bonus-points-spent 16, :ability-points-remaining 0}
                       (lycreate/ability-info
                         {::lyelm/chartype          :solar
                          ::lyelm/favored-abilities lyelm/abilities
                          ::lyelm/caste-abiliites   lyelm/abilities
                          ::lyelm/ability-ranks
                                                    {:lore        3, :melee 3, :investigation 3, :dodge 3, :presence 3,
                                                     :performance 3, :linguistics 3, :war 3, :socialize 3, :survival 3,
                                                     :bureaucracy 1, :occult 2, :resistance 3, :brawl 0, :ride 0,
                                                     :athletics   0, :thrown 0, :archery 0, :integrity 0, :awareness 0,
                                                     :larceny     0, :sail 0, :medicine 0, :stealth 0}})))
          (it "knows that favored or selected caste abilities are purchased for 1 bonus point, while unselected are
          purchased for two"
              (should= {:bonus-points-spent 6, :ability-points-remaining 0}
                       (lycreate/ability-info
                         {::lyelm/chartype          :solar
                          ::lyelm/caste             :dawn
                          ::lyelm/favored-abilities #{:archery}
                          ::lyelm/caste-abiliites   #{:performance}
                          ::lyelm/ability-ranks
                                                    {:investigation 4, :dodge 4, ; NOT selected abilities
                                                     :archery       4, :performance 4, ; selected abiliites
                                                     :lore          3, :melee 3, :presence 3,
                                                     :linguistics   0, :war 3, :socialize 3, :survival 1,
                                                     :bureaucracy   0, :occult 0, :resistance 0, :brawl 0, :ride 0,
                                                     :athletics     0, :thrown 0, :integrity 0, :awareness 0,
                                                     :larceny       0, :sail 0, :medicine 0, :stealth 0}}))))

(def ability-rank-blocks
  {:all-nothing (zipmap lyelm/abilities (repeat 0))
   :all-low     (zipmap lyelm/abilities (repeat 1))
   :all-mid     (zipmap lyelm/abilities (repeat 3))
   :all-max     (zipmap lyelm/abilities (repeat 5))})

(def merit-chron
  {:merits
   {"single rank"      {::lycmerit/name           "single rank"
                        ::lycmerit/possible-ranks [2]}
    "multiple ranks"   {::lycmerit/name           "multiple ranks"
                        ::lycmerit/possible-ranks [1 2 4]}
    "single prereq"    {::lycmerit/name           "prereq athletics 3"
                        ::lycmerit/possible-ranks [2]
                        ::lycmerit/prereq         [:or [:athletics 3]]}
    "multiple prereqs" {::lycmerit/name           "multiple prereqs"
                        ::lycmerit/possible-ranks [2]
                        ::lycmerit/prereq         [:or [:melee 3] [:brawl 3]]}}})

(describe "Validating Merits During Creation"
          (it "Knows that merit ranks cost merit points"
              (should= {:merit-points-remaining 2 :bonus-points-spent 0}
                       0
                       ;
                       ;(lycreate/merit-info {})
                       )))