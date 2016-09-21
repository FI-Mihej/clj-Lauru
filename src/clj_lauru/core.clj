;;;; Copyright © 2016 ButenkoMS. All rights reserved. Contacts: <gtalk@butenkoms.space>
;;;;
;;;; Licensed under the Apache License, Version 2.0 (the "License");
;;;; you may not use this file except in compliance with the License.
;;;; You may obtain a copy of the License at
;;;;
;;;;     http://www.apache.org/licenses/LICENSE-2.0
;;;;
;;;; Unless required by applicable law or agreed to in writing, software
;;;; distributed under the License is distributed on an "AS IS" BASIS,
;;;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;;;; See the License for the specific language governing permissions and
;;;; limitations under the License.

(ns clj-lauru.core
  (:require [clojure.string :as st]))

; ==============================================================================
; === GENERIC ==================================================================

(defonce uri-allowed-chars "\\w\\-\\~\\%\\.")

; :type
; from
; "list=?type"
;
; :user
; from
; "?user"
(defn param-to-keyword [pattern]
  (keyword (re-find (re-matcher (re-pattern (str "(?<=\\?)[" uri-allowed-chars "]+(?=$)")) pattern))))

; "https\\:\\/\\/dribbble\\.com\\/shots\\/1905065\\-Travel\\-Icons\\-pack\\?list\\=users"
; from
; "https://dribbble.com/shots/1905065-Travel-Icons-pack?list=users"
;
; "my\\-domain\\.com\\.eu"
; from
; "my-domain.com.eu"
(defn prepare-string-to-re-pattern [original-string]
  (st/replace original-string #"\W" #(str "\\" %1)))

; ==============================================================================
; === HOST =====================================================================

; "dribbble.com"
; from
; "host(dribbble.com); path(?user/status/?id); queryparam(offset=?offset); queryparam(list=?type);"
;
; ""
; from
; "host(); path(?user/status/?id); queryparam(offset=?offset); queryparam(list=?type);"
;
; nil
; from
; "path(?user/status/?id); queryparam(offset=?offset); queryparam(list=?type);"
(defn host-name [pattern]
  (let [inner-empty-host? (some? (re-find #"host\(\)" pattern))]
    (if inner-empty-host? 
        ""
        (re-find (re-matcher (re-pattern (str "(?<=host\\()[" uri-allowed-chars "]+(?=\\))")) pattern)))))

; #"^(http|https)://dribbble\.com($|\/$|\/.+|\#$|\#.+)"
; from
; "dribbble.com"
;
; #"^(http|https)://[\w\-\~\%\.]+($|\/$|\/.+|\#$|\#.+)"
; from
; ""
(defn host-pattern [host-name]
  (if (some? host-name) 
      (if (> (count host-name) 0) 
          (re-pattern (str "^(http|https)://" (prepare-string-to-re-pattern host-name) "($|\\/$|\\/.+|\\#$|\\#.+)"))
          (re-pattern (str "^(http|https)://[" uri-allowed-chars "]+($|\\/$|\\/.+|\\#$|\\#.+)")))
      ))

; #"^(http|https)://dribbble\.com($|\/$|\/.+|\#$|\#.+)"
; from
; "host(dribbble.com); path(?user/status/?id); queryparam(offset=?offset); queryparam(list=?type);"
(defn host-info [pattern]
  (let [inner-host-name (host-name pattern)]
    (if (some? inner-host-name) (host-pattern inner-host-name))))

; true
; from
; (pattern-info "host(dribbble.com); path(?user/status/?id); queryparam(offset=?offset); queryparam(list=?type);")
; "https://dribbble.com/shots/1905065-Travel-Icons-pack?list=users&offset=1"
;
; false
; from
; (pattern-info "host(dribbble.com); path(?user/status/?id); queryparam(offset=?offset); queryparam(list=?type);")
; "https://twitter.com/shots/1905065-Travel-Icons-pack?list=users&offset=1"
(defn check-host-by-info [pattern uri]
  (some? (re-matches pattern uri)))

; ==============================================================================
; === PATH =====================================================================

; "?user/status/?id"
; from
; "host(dribbble.com); path(?user/status/?id); queryparam(offset=?offset); queryparam(list=?type);"
(defn path [pattern]
  (re-find (re-matcher (re-pattern (str "(?<=path\\()[" uri-allowed-chars "\\?\\/]+(?=\\))")) pattern)))

; ["?user" "status" "?id"]
; from
; "?user/status/?id"
(defn path-to-pieces [inner-path-pattern]
  (st/split inner-path-pattern #"/"))

; "status"
; from
; "status"
;
; nil
; from
; "?status"
(defn path-piece-name [path-piece]
  (re-matches (re-pattern (str "[" uri-allowed-chars "]+")) path-piece))

; [[0 :user nil] [1 nil "status"] [2 :id nil]]
; from
; "?user/status/?id"
(defn path-param-info [inner-path-pattern]
  (let [path-pieces (path-to-pieces inner-path-pattern)]
    (vec (map-indexed (fn [num item] [num (param-to-keyword item) (path-piece-name item)]) path-pieces))))

; #"(?<=^(?:http|https)://dribbble\.com\/)[\w\-\~\%\.\/]+(?=\?)"
; from
; "dribbble.com"
;
; #"(?<=^(?:http|https)://[\w\-\~\%\.]+\/)[\w\-\~\%\.\/]+(?=\?)"
; from
; "dribbble.com"
(defn path-pattern [host-name]
  (if (some? host-name) 
      (if (> (count host-name) 0)
          (re-pattern (str "(?<=^(?:http|https)://" (prepare-string-to-re-pattern host-name) "\\/)[" uri-allowed-chars "\\/]+(?=\\?)")))))

(defonce path-prefix-pattern #"^(?:http|https)://[\w\-\~\%\.]+(?=.+)")

(defonce path-postfix-pattern (re-pattern (str "(?<=\\/)[" uri-allowed-chars "\\/]+(?=\\?)")))

; {:host-was-defined true, :path-pattern #"(?<=^(?:http|https)://dribbble\.com\/)[\w\-\~\%\.\/]+(?=\?)", :path-pieces [[0 :user nil] [1 nil "status"] [2 :id nil]]}
; from
; "host(dribbble.com); path(?user/status/?id); queryparam(offset=?offset); queryparam(list=?type);"
(defn path-info [pattern]
  (let [inner-path (path pattern)
        inner-host-name (host-name pattern)
        inner-host-name-defined? (if (some? inner-host-name) (if (> (count inner-host-name) 0) true false))
        inner-path-pattern (if (some? inner-path) (path-pattern inner-host-name))
        inner-path-prefix-pattern ()]
    (if (some? inner-path) {
      :host-was-defined inner-host-name-defined?
      :path-pattern inner-path-pattern
      :path-pieces (path-param-info inner-path)})))

; "shots/1905065-Travel-Icons-pack"
; from
; "https://dribbble.com/shots/1905065-Travel-Icons-pack?list=users&offset=1"
; #"(?<=^(?:http|https)://dribbble\.com\/)[\w\-\~\%\.\/]+(?=\?)"
(defn get-path-with-info [uri pattern]
  (re-find (re-matcher pattern uri)))

; "shots/1905065-Travel-Icons-pack"
; from
; "https://dribbble.com/shots/1905065-Travel-Icons-pack?list=users&offset=1"
; #"(?<=^(?:http|https)://dribbble\.com\/)[\w\-\~\%\.\/]+(?=\?)"
(defn get-path-with-info-for-undefined-host [uri]
  (let [inner-host-part (re-find (re-matcher path-prefix-pattern uri))
        inner-host-part-size (count inner-host-part)
        inner-post-host-part (subs uri inner-host-part-size)]
    (re-find (re-matcher path-postfix-pattern inner-post-host-part))))

; [[:user "some-username"] [:id "1905065-Travel-Icons-pack"]]
; from
; "https://dribbble.com/some-username/status/1905065-Travel-Icons-pack?list=users&offset=1"
; {:host-was-defined true, :path-pattern #"(?<=^(?:http|https)://dribbble\.com\/)[\w\-\~\%\.\/]+(?=\?)", :path-pieces [[0 :user nil] [1 nil "status"] [2 :id nil]]}
(defn generate-path-answer [uri inner-path-info]
  (let [can-be? (some? inner-path-info)
        path-pattern-good (if can-be? (some? (:host-was-defined inner-path-info)))
        path-pattern-defined (if path-pattern-good (:host-was-defined inner-path-info))
        inner-relative-part (if path-pattern-good (if path-pattern-defined 
                                                      (get-path-with-info uri (:path-pattern inner-path-info))
                                                      (get-path-with-info-for-undefined-host uri)))
        inner-path-pieces (if (some? inner-relative-part) (path-to-pieces inner-relative-part))
        inner-pieces-info (if can-be? (:path-pieces inner-path-info))
        inner-is-enough-pieces? (if can-be? (>= (count inner-path-pieces) (count inner-pieces-info)) false)
        inner-is-good-to-go? (and can-be? inner-is-enough-pieces?)]
    (if can-be? 
      (if inner-is-enough-pieces?
        (remove nil? (vec (map #((fn [path-pieces piece-info] 
            (if (some? (get piece-info 1)) 
              [
                (get piece-info 1) 
                (get path-pieces (get piece-info 0))
              ]
              (if (not (= (get piece-info 2) (get path-pieces (get piece-info 0))))
                [nil nil])
            )
          ) inner-path-pieces %) inner-pieces-info)))
        [nil]))))

; ==============================================================================
; === QUERYPARAMS ==============================================================

; ["offset=?offset" "list=?type"]
; from
; "host(dribbble.com); path(?user/status/?id); queryparam(offset=?offset); queryparam(list=?type);"
(defn queryparams [pattern]
  (let [inner-result (re-seq (re-pattern (str "(?<=queryparam\\()[" uri-allowed-chars "\\?\\=\\/]+(?=\\))")) pattern)]
    (if (some? inner-result) (vec inner-result))))

; "list"
; from
; "list=?type"
(defn query-param-name [pattern]
  (re-find (re-matcher (re-pattern (str "(?<=^)[" uri-allowed-chars "]+(?=\\=\\?)")) pattern)))

; #"(?<=[\?\&]list\=)[\w\-\%]+(?=[$\&\#])"
; from
; "list"
(defn query-param-pattern [q-param-name]
  (if (some? q-param-name) (re-pattern (str "(?<=[\\?\\&]" q-param-name "\\=)[\\w\\-\\%]+(?=[$\\&\\#])"))))

; [:type #"(?<=[\?\&]list\=)[\w\-\%]+(?=[$\&\#])"]
; from
; "list=?type"
(defn query-param-info [pattern]
  (let [inner-keyword (param-to-keyword pattern)
        inner-query-param-pattern (query-param-pattern (query-param-name pattern))
        inner-good-to-go? (and (some? inner-keyword) (some? inner-query-param-pattern))]
    (if inner-good-to-go? [inner-keyword inner-query-param-pattern])))

; [[:offset #"(?<=[\?\&]offset\=)[\w\-\%]+(?=[$\&\#])"] [:type #"(?<=[\?\&]list\=)[\w\-\%]+(?=[$\&\#])"]]
; from
; "host(dribbble.com); path(?user/status/?id); queryparam(offset=?offset); queryparam(list=?type);"
(defn query-info [pattern]
  (let [inner-queryparams (queryparams pattern)
        inner-result (if (some? inner-queryparams) (remove nil? (vec (map query-param-info inner-queryparams))))]
    (if (some? inner-result)
        (if (> (count inner-result) 0) inner-result))))

; "users"
; from
; "https://twitter.com/shots/1905065-Travel-Icons-pack?list=users&offset=1"
; #"(?<=[\?\&]list\=)[\w\-\%]+(?=[$\&\#])"
(defn get-query-param-with-info [uri pattern]
  (re-find (re-matcher pattern uri)))

; [[:list "users"] [:offset "1"]]
; from
; "https://twitter.com/shots/1905065-Travel-Icons-pack?list=users&offset=1#some-my/fragment"
; [[:offset #"(?<=[\?\&]offset\=)[\w\-\%]+(?=[$\&\#])"] [:type #"(?<=[\?\&]list\=)[\w\-\%]+(?=[$\&\#])"]]
(defn generate-query-answer [uri inner-query-info]
  (if (some? inner-query-info)
    (vec (map #((fn [uri param-info] [(get param-info 0) (get-query-param-with-info uri (get param-info 1))]) uri %) inner-query-info))))

; ==============================================================================
; === FRAGMENT ==============================================================

; "?paragraph"
; from
; "host(dribbble.com); path(?user/status/?id); queryparam(offset=?offset); queryparam(list=?type); fragment(?paragraph)"
(defn fragment [pattern]
  (re-find (re-matcher (re-pattern (str "(?<=fragment\\()[" uri-allowed-chars "\\?]+(?=\\))")) pattern)))

; #"(?<=\#).+(?=$)"
(defn fragment-pattern []
  #"(?<=\#).+(?=$)")

; [:paragraph #"(?<=\#).+(?=$)"]
; from
; "host(dribbble.com); path(?user/status/?id); queryparam(offset=?offset); queryparam(list=?type); fragment(?paragraph)"
(defn fragment-info [pattern]
  (let [inner-fragment (fragment pattern)
        inner-keyword (if (some? inner-fragment) (param-to-keyword inner-fragment))
        inner-good-to-go? (and (some? inner-fragment) (some? inner-keyword))]
    (if inner-good-to-go? [inner-keyword (fragment-pattern)])))

; "some-my/fragment"
; from
; "https://twitter.com/shots/1905065-Travel-Icons-pack?list=users&offset=1#some-my/fragment"
; #"(?<=\#).+(?=$)"
(defn get-fragment-with-info [uri pattern]
  (re-find (re-matcher pattern uri)))

; [[:paragraph "some-my/fragment"]]
; from
; "https://twitter.com/shots/1905065-Travel-Icons-pack?list=users&offset=1#some-my/fragment"
; [:paragraph #"(?<=\#).+(?=$)"]
(defn generate-fragment-answer [uri inner-fragment-info]
  (if (some? inner-fragment-info)
    [[(get inner-fragment-info 0) (get-fragment-with-info uri (get inner-fragment-info 1))]]))

; ==============================================================================
; === API ======================================================================

; {:host #"^(http|https)://dribbble\.com($|\/$|\/.+|\#$|\#.+)", :path {:path-pattern #"(?<=^(?:http|https)://dribbble\.com\/)[\w\-\~\%\.\/]+(?=\?)", :path-pieces [[0 :user nil] [1 nil "status"] [2 :id nil]]}, :query [[:offset #"(?<=[\?\&]offset\=)[\w\-\%]+(?=[$\&\#])"] [:type #"(?<=[\?\&]list\=)[\w\-\%]+(?=[$\&\#])"]], :fragment [:paragraph #"(?<=\#).+(?=$)"]}
; from
; "host(dribbble.com); path(?user/status/?id); queryparam(offset=?offset); queryparam(list=?type); fragment(?paragraph)"
(defn pattern-info [pattern]
  { :host (host-info pattern)
    :path (path-info pattern)
    :query (query-info pattern)
    :fragment (fragment-info pattern)})

; [[:user "some-username"] [:id "1905065-Travel-Icons-pack"] [:offset "1"] [:type "users"] [:paragraph "paragraph=3"]]
; from
; (pattern-info "host(dribbble.com); path(?user/status/?id); queryparam(offset=?offset); queryparam(list=?type); fragment(?paragraph)")
; "https://dribbble.com/some-username/status/1905065-Travel-Icons-pack?list=users&offset=1&page=34#paragraph=3"
;
; nil
; from
; (pattern-info "host(dribbble.com); path(?user/status/?id); queryparam(offset=?offset); queryparam(list=?type); fragment(?paragraph)")
; "https://twitter.com/some-username/status/1905065-Travel-Icons-pack?list=users&offset=1&page=34#paragraph=3"
;
; [[:user "some-username"] [nil nil] [:id "weight"] [:offset "1"] [:type nil] [:paragraph nil]]
; from
; (pattern-info "host(dribbble.com); path(?user/status/?id); queryparam(offset=?offset); queryparam(list=?type); fragment(?paragraph)")
; "https://dribbble.com/some-username/height/weight/1905065-Travel-Icons-pack?listing=users&offset=1&page=34"
(defn recognize-detailed [pattern uri]
  (let [inner-host-info (:host pattern)
        is-host-ok? (if (some? inner-host-info)
                        (check-host-by-info inner-host-info uri)
                        true)
        inner-path-info (:path pattern)
        inner-query-info (:query pattern)
        inner-fragment-info (:fragment pattern)]
    (if is-host-ok?
      (vec (concat
        (generate-path-answer uri inner-path-info)
        (generate-query-answer uri inner-query-info)
        (generate-fragment-answer uri inner-fragment-info))))))

; [[:user "some-username"] [:id "1905065-Travel-Icons-pack"] [:offset "1"] [:type "users"] [:paragraph "paragraph=3"]]
; from
; (pattern-info "host(dribbble.com); path(?user/status/?id); queryparam(offset=?offset); queryparam(list=?type); fragment(?paragraph)")
; "https://dribbble.com/some-username/status/1905065-Travel-Icons-pack?list=users&offset=1&page=34#paragraph=3"
;
; nil
; from
; (pattern-info "host(dribbble.com); path(?user/status/?id); queryparam(offset=?offset); queryparam(list=?type); fragment(?paragraph)")
; "https://twitter.com/some-username/status/1905065-Travel-Icons-pack?list=users&offset=1&page=34#paragraph=3"
;
; nil
; from
; (pattern-info "host(dribbble.com); path(?user/status/?id); queryparam(offset=?offset); queryparam(list=?type); fragment(?paragraph)")
; "https://dribbble.com/some-username/height/weight/1905065-Travel-Icons-pack?listing=users&offset=1&page=34"
(defn recognize [pattern uri]
  (let [detailed-result (recognize-detailed pattern uri)]
    (if (some? detailed-result)
        (if (not (contains? (set (flatten detailed-result)) nil))
            detailed-result))))
