(ns lytek.character-creation-validation-spec
  (:require [speclj.core :refer :all]
            [lytek.core :refer :all]
            [lytek.character.elements :as lyelm]
            [lytek.validation.creation :as lycreate]
            [lytek.validation :as lyval]
            [lytek.testing-character-elements :as testelm]))

(describe "the test"
          (it "FIXME, I fail."
              (should= 1 1)))

(describe "Validating Attributes During Creation"
          (it "properly determines when attributes have been fully purchased without bonus points"
              (should= {:bonus-points-spent 0, :attribute-points-remaining {:mental 0, :social 0, :physical 0}}
                       (lycreate/validate-attributes
                         {:chartype :solar
                          :attribute-ranks
                                    {:strength   5 :dexterity 5 :stamina 1
                                     :appearance 3 :charisma 3 :manipulation 3
                                     :wits       3 :intelligence 3 :perception 1}}
                         nil)))
          (it "knows when there are points remaining"
              (should= {:bonus-points-spent 0, :attribute-points-remaining {:mental 1, :social 2, :physical 3}}
                       (lycreate/validate-attributes
                         {:chartype :solar
                          :attribute-ranks
                                    {:strength   2 :dexterity 5 :stamina 1
                                     :appearance 1 :charisma 3 :manipulation 3
                                     :wits       2 :intelligence 3 :perception 1}}
                         nil)))
          (it "knows when attributes have used bonus points"
              (should= {:bonus-points-spent 23, :attribute-points-remaining {:mental 0, :social 0, :physical 0}}
                       (lycreate/validate-attributes
                         {:chartype :solar
                          :attribute-ranks
                                    {:strength   5 :dexterity 5 :stamina 4
                                     :appearance 4 :charisma 4 :manipulation 3
                                     :wits       4 :intelligence 3 :perception 1}}
                         nil))))

(describe "Validating Solar-spcific information during creation"
          (str "While many things differentiating certain kinds of characters are easily handled by mildly tweaking "
               "the general validation functions, some features are genuinely exalt-type-spcific. These things "
               "are inappropriate to shoe-horn into another validation function but are a perfect candidate for "
               "multi-method usage! As solars are the only supported character type as of writing this test, "
               "solars are all that will be tested here.")
          (it "knows that only certain abilites can be selected as caste abilities"
              (should= [[[:performance] :caste-abiliites]]
                       (lycreate/validate-exalt-spcific
                         {:chartype          :solar
                          :caste             :dawn
                          :favored-abilities #{}
                          :caste-abiliites   #{:performance}}
                         nil))
              (should= {:caste-abilities-remaining   4
                        :favored-abilities-remaining 5
                        :supernal-needs-selecting?   true}
                       (lycreate/validate-exalt-spcific
                         {:chartype          :solar
                          :caste             :dawn
                          :favored-abilities #{}
                          :caste-abiliites   #{:archery}}
                         nil)))
          (it "knows that only abilities that are *not* selected as caste abilities can be selected as favored abilities"
              (should= [[[:archery] :favored-abilities]]
                       (lycreate/validate-exalt-spcific
                         {:chartype          :solar
                          :caste             :dawn
                          :favored-abilities #{:archery}
                          :caste-abiliites   #{:archery}}
                         nil))
              (should= {:caste-abilities-remaining   4
                        :favored-abilities-remaining 4
                        :supernal-needs-selecting?   true}
                       (lycreate/validate-exalt-spcific
                         {:chartype          :solar
                          :caste             :dawn
                          :favored-abilities #{:performance}
                          :caste-abiliites   #{:archery}}
                         nil)))
          (it "knows that the ability selected as \"supernal\" must have been selected as a caste ability"
              (should= [[:archery :supernal]]
                       (lycreate/validate-exalt-spcific
                         {:chartype          :solar
                          :caste             :dawn
                          :supernal          :archery
                          :favored-abilities #{}
                          :caste-abiliites   #{}}
                         nil))
              (should= {:caste-abilities-remaining   4
                        :favored-abilities-remaining 4
                        :supernal-needs-selecting?   false}
                       (lycreate/validate-exalt-spcific
                         {:chartype          :solar
                          :caste             :dawn
                          :supernal          :archery
                          :favored-abilities #{:performance}
                          :caste-abiliites   #{:archery}}
                         nil))))

(describe "Validating Abilities During Creation"
          (it "knows when abilities have been purchased with no bonus points"
              (should= {:bonus-points-spent 0, :ability-points-remaining 1}
                       (lycreate/validate-abilities
                         {:chartype :solar
                          :ability-ranks
                                    {:lore        3, :melee 2, :investigation 3, :dodge 3, :presence 3,
                                     :performance 3, :linguistics 3, :war 3, :socialize 3, :survival 1,
                                     :bureaucracy 0, :occult 0, :resistance 0, :brawl 0, :ride 0,
                                     :athletics   0, :thrown 0, :archery 0, :integrity 0, :awareness 0,
                                     :larceny     0, :sail 0, :medicine 0, :stealth 0}}
                         testelm/merit-chron)))
          (it "knows that any rank over 3 involves bonus points"
              (should= {:bonus-points-spent 4, :ability-points-remaining 0}
                       (lycreate/validate-abilities
                         {:chartype          :solar
                          :favored-abilities lyelm/abilities
                          :caste-abiliites   lyelm/abilities
                          :ability-ranks
                                             {:lore        5, :melee 3, :investigation 3, :dodge 5, :presence 3,
                                              :performance 3, :linguistics 3, :war 3, :socialize 3, :survival 1,
                                              :bureaucracy 0, :occult 0, :resistance 0, :brawl 0, :ride 0,
                                              :athletics   0, :thrown 0, :archery 0, :integrity 0, :awareness 0,
                                              :larceny     0, :sail 0, :medicine 0, :stealth 0}}
                         testelm/merit-chron)))
          (it "knows when there are points left to spend"
              (should= {:bonus-points-spent 0, :ability-points-remaining 3}
                       (lycreate/validate-abilities
                         {:chartype :solar
                          :ability-ranks
                                    {:lore        2, :melee 2, :investigation 2, :dodge 3, :presence 3,
                                     :performance 3, :linguistics 3, :war 3, :socialize 3, :survival 1,
                                     :bureaucracy 0, :occult 0, :resistance 0, :brawl 0, :ride 0,
                                     :athletics   0, :thrown 0, :archery 0, :integrity 0, :awareness 0,
                                     :larceny     0, :sail 0, :medicine 0, :stealth 0}}
                         testelm/merit-chron)))
          (it "knows when bonus points have been spent while also having spent bonus points"
              (should= {:bonus-points-spent 4, :ability-points-remaining 3}
                       (lycreate/validate-abilities
                         {:chartype          :solar
                          :favored-abilities lyelm/abilities
                          :caste-abiliites   lyelm/abilities
                          :ability-ranks
                                             {:lore        2, :melee 2, :investigation 2, :dodge 5, :presence 5,
                                              :performance 3, :linguistics 3, :war 3, :socialize 3, :survival 1,
                                              :bureaucracy 0, :occult 0, :resistance 0, :brawl 0, :ride 0,
                                              :athletics   0, :thrown 0, :archery 0, :integrity 0, :awareness 0,
                                              :larceny     0, :sail 0, :medicine 0, :stealth 0}}
                         testelm/merit-chron)))
          (it "knows that, even when no attributes are over 3, more then 28 points total involves bonus points"
              (should= {:bonus-points-spent 8, :ability-points-remaining 0}
                       (lycreate/validate-abilities
                         {:chartype          :solar
                          :favored-abilities lyelm/abilities
                          :caste-abiliites   lyelm/abilities
                          :ability-ranks
                                             {:lore        3, :melee 3, :investigation 3, :dodge 3, :presence 3,
                                              :performance 3, :linguistics 3, :war 3, :socialize 3, :survival 3,
                                              :bureaucracy 1, :occult 2, :resistance 3, :brawl 0, :ride 0,
                                              :athletics   0, :thrown 0, :archery 0, :integrity 0, :awareness 0,
                                              :larceny     0, :sail 0, :medicine 0, :stealth 0}}
                         testelm/merit-chron)))
          (it "knows that favored or selected caste abilities are purchased for 1 bonus point, while unselected are
          purchased for two"
              (should= {:bonus-points-spent 6, :ability-points-remaining 0}
                       (lycreate/validate-abilities
                         {:chartype          :solar
                          :caste             :dawn
                          :favored-abilities #{:archery}
                          :caste-abiliites   #{:performance}
                          :ability-ranks
                                             {:investigation 4, :dodge 4, ; NOT selected abilities
                                              :archery       4, :performance 4, ; selected abiliites
                                              :lore          3, :melee 3, :presence 3,
                                              :linguistics   0, :war 3, :socialize 3, :survival 1,
                                              :bureaucracy   0, :occult 0, :resistance 0, :brawl 0, :ride 0,
                                              :athletics     0, :thrown 0, :integrity 0, :awareness 0,
                                              :larceny       0, :sail 0, :medicine 0, :stealth 0}}
                         testelm/merit-chron))))


(describe "Validating Merits During Creation"
          (it "Knows that merit ranks cost merit points at 1:1"
              (should= {:merit-points-remaining 8 :bonus-points-spent 0}
                       (lycreate/validate-merits
                         {:chartype      :solar
                          :caste         :dawn
                          :ability-ranks (testelm/ability-rank-blocks :all-nothing)
                          :merits        [["single rank" 2]]}
                         testelm/merit-chron)))
          (it "Knows that a character can only have certain ranks in certain merits"
              (should= [[["single rank" 1] :possible-ranks]]
                       (lycreate/validate-merits
                         {:chartype      :solar
                          :caste         :dawn
                          :ability-ranks (testelm/ability-rank-blocks :all-nothing)
                          :merits        [["single rank" 1]]}
                         testelm/merit-chron)))
          (it "Knows that a character can only have a merit if they meet the prerequisites"
              (should= [[["prereq athletics 3" 1] :prereq]]
                       (lycreate/validate-merits
                         {:chartype      :solar
                          :caste         :dawn
                          :ability-ranks (testelm/ability-rank-blocks :all-nothing)
                          :merits        [["prereq athletics 3" 1]]}
                         testelm/merit-chron))
              (should= {:merit-points-remaining 9 :bonus-points-spent 0}
                       (lycreate/validate-merits
                         {:chartype      :solar
                          :caste         :dawn
                          :ability-ranks (assoc (testelm/ability-rank-blocks :all-nothing) :athletics 3)
                          :merits        [["prereq athletics 3" 1]]}
                         testelm/merit-chron))
              (should= {:merit-points-remaining 9 :bonus-points-spent 0}
                       (lycreate/validate-merits
                         {:chartype      :solar
                          :caste         :dawn
                          :ability-ranks (assoc (testelm/ability-rank-blocks :all-nothing) :brawl 3)
                          :merits        [["multiple or prereqs" 1]]}
                         testelm/merit-chron))
              (should= [[["multiple and prereqs" 1] :prereq]]
                       (lycreate/validate-merits
                         {:chartype      :solar
                          :caste         :dawn
                          :ability-ranks (assoc (testelm/ability-rank-blocks :all-nothing) :brawl 3)
                          :merits        [["multiple and prereqs" 1]]}
                         testelm/merit-chron))
              (should= {:merit-points-remaining 9 :bonus-points-spent 0}
                       (lycreate/validate-merits
                         {:chartype      :solar
                          :caste         :dawn
                          :ability-ranks (assoc (testelm/ability-rank-blocks :all-nothing) :brawl 3 :melee 3)
                          :merits        [["multiple and prereqs" 1]]}
                         testelm/merit-chron)))
          (it "Knows that a merit must have repurchasable set to true in order to have multiple instances in a character."
              (should= {:merit-points-remaining 7 :bonus-points-spent 0}
                       (lycreate/validate-merits
                         {:chartype      :solar
                          :caste         :dawn
                          :ability-ranks (testelm/ability-rank-blocks :all-nothing)
                          :merits        [["repurchasable" 1] ["repurchasable" 2]]}
                         testelm/merit-chron))
              (should= [[["single rank" 0] :repurchasable]]
                       (lycreate/validate-merits
                         {:chartype      :solar
                          :caste         :dawn
                          :ability-ranks (testelm/ability-rank-blocks :all-nothing)
                          :merits        [["single rank" 2] ["single rank" 2]]}
                         testelm/merit-chron)))
          (it "Knows that after 10 merit points are spent, a normal solar will began spending bonus points for merits at 1:1"
              (should= {:merit-points-remaining 0 :bonus-points-spent 3}
                       (lycreate/validate-merits
                         {:chartype      :solar
                          :caste         :dawn
                          :ability-ranks (testelm/ability-rank-blocks :all-nothing)
                          :merits        [["repurchasable" 5] ["repurchasable" 5] ["repurchasable" 3]]}
                         testelm/merit-chron)))
          (it "Can handle multiple kinds of merits all in the same character at once"
              (should= {:merit-points-remaining 0 :bonus-points-spent 5}
                       (lycreate/validate-merits
                         {:chartype      :solar
                          :caste         :dawn
                          :ability-ranks (assoc (testelm/ability-rank-blocks :all-nothing) :brawl 3 :athletics 3)
                          :merits        [["repurchasable" 5] ["repurchasable" 3] ["single rank" 2]
                                          ["multiple or prereqs" 2] ["prereq athletics 3" 3]]}
                         testelm/merit-chron))))

(describe "Validating Charms During Creation"
          (it "Knows that charms cost charm points"
              (should= {:charm-points-remaining 14 :bonus-points-spent 0}
                       (lycreate/validate-charms
                         {:chartype      :solar
                          :caste         :dawn
                          :ability-ranks (testelm/ability-rank-blocks :all-low)
                          :merits        [["single rank" 2]]
                          :charms        [["simple charm" "nothing to note"]]}
                         (into testelm/merit-chron testelm/charm-chron))))
          (it "Knows that a solar must meet minimum ability requirements in order to have a charm"
              (should= [["higher-ability charm" :prereq-stats]]
                       (lycreate/validate-charms
                         {:chartype      :solar
                          :caste         :dawn
                          :ability-ranks (testelm/ability-rank-blocks :all-low)
                          :merits        [["single rank" 2]]
                          :charms        [["higher-ability charm" "nothing to note"]]}
                         (into testelm/merit-chron testelm/charm-chron)))
              (should= {:charm-points-remaining 14, :bonus-points-spent 0}
                       (lycreate/validate-charms
                         {:chartype      :solar
                          :caste         :dawn
                          :ability-ranks (assoc (testelm/ability-rank-blocks :all-low) :athletics 3)
                          :merits        [["single rank" 2]]
                          :charms        [["higher-ability charm" "nothing to note"]]}
                         (into testelm/merit-chron testelm/charm-chron))))
          (it (str "Knows that, if the charm requires essence > 1, "
                   "a Solar can't have it at creation unless its required ability is supernal")
              (should= [["higher-essence charm" :supernal]]
                       (lycreate/validate-charms
                         {:chartype        :solar
                          :caste           :dawn
                          :caste-abiliites #{:athletics :melee}
                          :supernal        :melee
                          :ability-ranks   (testelm/ability-rank-blocks :all-low)
                          :merits          [["single rank" 2]]
                          :charms          [["higher-essence charm" "nothing to note"]]}
                         (into testelm/merit-chron testelm/charm-chron)))
              (should= {:charm-points-remaining 14, :bonus-points-spent 0}
                       (lycreate/validate-charms
                         {:chartype        :solar
                          :caste           :dawn
                          :caste-abiliites #{:athletics :melee}
                          :supernal        :athletics
                          :ability-ranks   (testelm/ability-rank-blocks :all-low)
                          :merits          [["single rank" 2]]
                          :charms          [["higher-essence charm" "nothing to note"]]}
                         (into testelm/merit-chron testelm/charm-chron))))
          (it (str "Knows that bonus points are used after 15 charms are selected at a"
                   "rate of 1 per caste/favored, 2 per not, and that charm slots are stored positionally")
              (should= {:charm-points-remaining 0, :bonus-points-spent 5}
                       (lycreate/validate-charms
                         {:chartype        :solar
                          :caste           :dawn
                          :caste-abilities #{:athletics :melee}
                          :supernal        :athletics
                          :ability-ranks   (testelm/ability-rank-blocks :all-low)
                          :merits          [["single rank" 2]]
                          :charms          (testelm/make-bulk-charm-slots "athleto-bulk" 20)}
                         (into testelm/merit-chron testelm/charm-chron)))
              (should= {:charm-points-remaining 0, :bonus-points-spent 10}
                       (lycreate/validate-charms
                         {:chartype        :solar
                          :caste           :dawn
                          :caste-abilities #{:athletics :melee}
                          :supernal        :athletics
                          :ability-ranks   (testelm/ability-rank-blocks :all-low)
                          :merits          [["single rank" 2]]
                          :charms          (testelm/make-bulk-charm-slots "performo-bulk" 20)}
                         (into testelm/merit-chron testelm/charm-chron)))
              (should= {:charm-points-remaining 0, :bonus-points-spent 7}
                       (lycreate/validate-charms
                         {:chartype        :solar
                          :caste           :dawn
                          :caste-abilities #{:athletics :melee}
                          :supernal        :athletics
                          :ability-ranks   (testelm/ability-rank-blocks :all-low)
                          :merits          [["single rank" 2]]
                          :charms          (interleave (testelm/make-bulk-charm-slots "performo-bulk" 10)
                                                       (testelm/make-bulk-charm-slots "athleto-bulk" 10))}
                         (into testelm/merit-chron testelm/charm-chron))))
          (it "Knows that charms require their prerequisites to be present earlier in the list to be valid"
              (should= {:charm-points-remaining 13, :bonus-points-spent 0}
                       (lycreate/validate-charms
                         {:chartype      :solar
                          :caste         :dawn
                          :ability-ranks (assoc (testelm/ability-rank-blocks :all-low) :athletics 3)
                          :merits        [["single rank" 2]]
                          :charms        [["simple charm" "nothing to note"] ["needs-one-prereq" "nothing to note"]]}
                         (into testelm/merit-chron testelm/charm-chron)))
              (should= [["needs-one-prereq" :prereq-charms]]
                       (lycreate/validate-charms
                         {:chartype      :solar
                          :caste         :dawn
                          :ability-ranks (assoc (testelm/ability-rank-blocks :all-low) :athletics 3)
                          :merits        [["single rank" 2]]
                          :charms        [["needs-one-prereq" "nothing to note"] ["simple charm" "nothing to note"]]}
                         (into testelm/merit-chron testelm/charm-chron)))
              (should= [["needs-one-prereq" :prereq-charms]]
                       (lycreate/validate-charms
                         {:chartype      :solar
                          :caste         :dawn
                          :ability-ranks (assoc (testelm/ability-rank-blocks :all-low) :athletics 3)
                          :merits        [["single rank" 2]]
                          :charms        (into (testelm/make-bulk-charm-slots "athleto-bulk" 2)
                                               [["needs-one-prereq" "nothing to note"] ["simple charm" "nothing to note"]])}
                         (into testelm/merit-chron testelm/charm-chron)))))

(describe "The Tagging Subsystem"
          (it (str "checks merits, charms, etc, and identifies possible tags")
              (should= [:blorp :blorp :kafwunka]
                       (lycreate/get-static-character-tags
                         {:chartype :solar
                          :caste    :dawn
                          :merits   [["adds sample tag" 1] ["adds sample tag" 2]]
                          :charms   [["gives a tag" "nothing to note"]]
                          }
                         (into testelm/merit-chron testelm/charm-chron))))
          (it (str "gets a different tag from a merit depending on the selected rank")
              (should= [:terciro :fourth-y :primero :fourth-y :full-circle]
                       (lycreate/get-static-character-tags
                         {:chartype :solar
                          :caste    :dawn
                          :merits   [["adds tag from rank" 3] ["adds tag from rank" 4]
                                     ["adds tag from rank" 1] ["adds tag from rank" 4]
                                     ["adds tag from rank" 5]]
                          :charms   [["simple charm" "nada"]]
                          }
                         (into testelm/merit-chron testelm/charm-chron)))))

(describe "Validating Martial Arts"
          (it (str "knows that martial art styles are represented in their own field, behave like ability points "
                   "and require the :martial-artist tag")
              (should= {:bonus-points-spent 0, :ability-points-remaining 1}
                       (lycreate/validate-abilities
                         {:chartype          :solar
                          :caste             :dawn
                          :attribute-ranks   (testelm/attribute-rank-blocks :all-low)
                          :ability-ranks     (testelm/ability-rank-blocks :all-low)
                          :favored-abilities #{}
                          :caste-abiliites   #{}
                          :merits            [["adds martial arts tag" 5]]
                          :martial-arts      [["tiger style" 3]]}
                         testelm/merit-chron))
              (should= {:bonus-points-spent 4, :ability-points-remaining 0}
                       (lycreate/validate-abilities
                         {:chartype          :solar
                          :caste             :dawn
                          :attribute-ranks   (testelm/attribute-rank-blocks :all-low)
                          :ability-ranks     (testelm/ability-rank-blocks :all-low)
                          :favored-abilities #{}
                          :caste-abiliites   #{}
                          :merits            [["adds martial arts tag" 5]]
                          :martial-arts      [["tiger style" 3] ["orchid style" 3]]}
                         testelm/merit-chron))
              (should= [:static-tags :martial-artist]
                       (lycreate/validate-abilities
                         {:chartype          :solar
                          :caste             :dawn
                          :attribute-ranks   (testelm/attribute-rank-blocks :all-low)
                          :ability-ranks     (testelm/ability-rank-blocks :all-low)
                          :favored-abilities #{}
                          :caste-abiliites   #{}
                          :merits            []
                          :martial-arts      [["tiger style" 3] ["orchid style" 3]]}
                         testelm/merit-chron)))
          (it (str "knows that charms with the martial-artist tag require a spcific style")
              (should= {:charm-points-remaining 14 :bonus-points-spent 0}
                       (lycreate/validate-charms
                         {:chartype     :solar
                          :caste        :dawn
                          :merits       [["adds martial arts tag" 5]]
                          :charms       [["hobo style starter" "nothing to note"]]
                          :martial-arts [["hobo style" 3]]}
                         (into testelm/charm-chron testelm/merit-chron)))
              (should= [["hobo style starter" :prereq-stats]]
                       (lycreate/validate-charms
                         {:chartype     :solar
                          :caste        :dawn
                          :merits       [["adds martial arts tag" 5]]
                          :charms       [["hobo style starter" "nothing to note"]]
                          :martial-arts []}
                         (into testelm/charm-chron testelm/merit-chron)))))

(describe "Validating Crafting"
          (it (str "knows that crafting focuses are represented in their own field, behave like ability points "
                   "but require no tag")
              (should= {:bonus-points-spent 0, :ability-points-remaining 1}
                       (lycreate/validate-abilities
                         {:chartype          :solar
                          :caste             :dawn
                          :attribute-ranks   (testelm/attribute-rank-blocks :all-low)
                          :ability-ranks     (testelm/ability-rank-blocks :all-low)
                          :favored-abilities #{}
                          :caste-abiliites   #{}
                          :crafts            [["masonry" 3]]}
                         testelm/merit-chron))
              (should= {:bonus-points-spent 2, :ability-points-remaining 0}
                       (lycreate/validate-abilities
                         {:chartype          :solar
                          :caste             :dawn
                          :attribute-ranks   (testelm/attribute-rank-blocks :all-low)
                          :ability-ranks     (testelm/ability-rank-blocks :all-low)
                          :favored-abilities #{}
                          :caste-abiliites   #{}
                          :crafts            [["woodworking" 3] ["artifice" 2]]}
                         testelm/merit-chron)))
          (it "knows that crafting charms use the highest focus rating to satasfy ability score prereqsuites"
              (should= {:charm-points-remaining 14 :bonus-points-spent 0}
                       (lycreate/validate-charms
                         {:chartype :solar
                          :caste    :dawn
                          :charms   [["awesome craft charm" "nothing to note"]]
                          :crafts   [["taxidermi" 2] ["roofing" 3]]}
                         (into testelm/charm-chron {})))
              (should= [["awesome craft charm" :prereq-stats]]
                       (lycreate/validate-charms
                         {:chartype :solar
                          :caste    :dawn
                          :charms   [["awesome craft charm" "nothing to note"]]
                          :crafts   [["taxidermi" 2] ["roofing" 2]]}
                         (into testelm/charm-chron {})))))

(def no-remaining-artifacts {:artifact-2    0 :artifact-3 0 :artifact-4 0 :artifact-5 0
                             :hearthstone-2 0 :hearthstone-4 0})

(describe "Inventory Validation"
          (it "knows that no tags and no artifacts is fine"
              (should= {:remaining no-remaining-artifacts}
                       (lycreate/validate-panoply
                         {:chartype :solar
                          :caste    :dawn
                          :panoply  []}
                         (into testelm/charm-chron testelm/merit-chron))))
          (it "knows that a character can have up to tags' worth of artifacts"
              (should= {:remaining (into no-remaining-artifacts {:artifact-4 1})}
                       (lycreate/validate-panoply
                         {:chartype :solar
                          :caste    :dawn
                          :merits   [["artifact tag giver" 3]
                                     ["artifact tag giver" 3]
                                     ["artifact tag giver" 4]]
                          :panoply  [{:name "great and mighty sword"
                                      :type :artifact}
                                     {:name "great and mighty sword"
                                      :type :artifact}
                                     {:name "quarry hammer"
                                      :type :mundane}]}
                         (conj testelm/charm-chron testelm/merit-chron testelm/panoply-chron))))
          (it "knows that a character can't have artifacts that they don't have sufficient tags for"
              (should= [[:static-tags :artifact-3]]
                       (lycreate/validate-panoply
                         {:chartype :solar
                          :caste    :dawn
                          :merits   [["artifact tag giver" 3]
                                     ["artifact tag giver" 4]]
                          :panoply  [{:name "great and mighty sword"
                                      :type :artifact}
                                     {:name "great and mighty sword"
                                      :type :artifact}]}
                         (conj testelm/charm-chron testelm/merit-chron testelm/panoply-chron)))))


