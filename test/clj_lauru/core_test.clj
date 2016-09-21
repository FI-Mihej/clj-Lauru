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

(ns clj-lauru.core-test
  (:require [clojure.test :refer :all]
            [clj-lauru.core :refer :all]))

(deftest url-match-tests
  (testing "URL-MATCH. Pattern Info."
    (is (=  (:fragment (pattern-info "host(some-domain.com); path(?user/status/?id); queryparam(offset=?offset); queryparam(list=?type); fragmmmment(?paragraph)"))
            nil))
    (is (=  (:fragment (pattern-info "host(some-domain.com); path(?user/status/?id); queryparam(offset=?offset); queryparam(list=?type); fragment(paragraph)"))
            nil))
    (is (=  (:fragment (pattern-info "host(some-domain.com); path(?user/status/?id); queryparam(offset=?offset); queryparam(list=?type)"))
            nil))
    (is (=  (:query (pattern-info "host(some-domain.com); path(?user/status/?id); querrrryparam(list=?type); fragment(?paragraph)"))
            nil))
    (is (=  (:query (pattern-info "host(some-domain.com); path(?user/status/?id); fragment(?paragraph)"))
            nil))
    (is (=  (:query (pattern-info "host(some-domain.com); path(?user/status/?id); queryparam(list=type); fragment(?paragraph)"))
            nil))
    (is (=  (:query (pattern-info "host(some-domain.com); path(?user/status/?id); queryparam(=?type); fragment(?paragraph)"))
            nil))
    (is (=  (:query (pattern-info "host(some-domain.com); path(?user/status/?id); queryparam(list=?); fragment(?paragraph)"))
            nil))
    (is (=  (:path (pattern-info "host(some-domain.com); queryparam(offset=?offset); queryparam(list=?type); fragment(?paragraph)"))
            nil))
    (is (=  (:path (pattern-info "host(some-domain.com); path(); queryparam(offset=?offset); queryparam(list=?type); fragment(?paragraph)"))
            nil))
    (is (=  (:host (pattern-info "host(http://some-domain.com); path(?user/status/?id); queryparam(offset=?offset); queryparam(list=?type); fragment(?paragraph)"))
            nil))
    (is (=  (:host (pattern-info "path(?user/status/?id); queryparam(offset=?offset); queryparam(list=?type); fragment(?paragraph)"))
            nil))
  )
  (testing "URL-MATCH. Bad uri."
    (is (= nil (recognize 
                  (pattern-info "host(some-domain.com); path(?user/status/?id); queryparam(offset=?offset); queryparam(list=?type); fragment(?paragraph)") 
                  "https://wrong-domain.com/some-username/status/1905065-Travel-Icons-pack?list=users&offset=1&page=34#paragraph=3")))
    (is (= nil (recognize 
                  (pattern-info "host(some-domain.com); path(?user/status/?id); queryparam(offset=?offset); queryparam(list=?type); fragment(?paragraph)") 
                  "https://some-domain.com/some-username/staaaaatus/1905065-Travel-Icons-pack?list=users&offset=1&page=34#paragraph=3")))
    (is (= nil (recognize 
                  (pattern-info "host(some-domain.com); path(?user/status/?id); queryparam(offset=?offset); queryparam(list=?type); fragment(?paragraph)") 
                  "https://some-domain.com/some-username/status?list=users&offset=1&page=34#paragraph=3")))
    (is (= nil (recognize 
                  (pattern-info "host(some-domain.com); path(?user/status/?id); queryparam(offset=?offset); queryparam(list=?type); fragment(?paragraph)") 
                  "https://some-domain.com/some-username/1905065-Travel-Icons-pack?list=users&offset=1&page=34#paragraph=3")))
    (is (= nil (recognize 
                  (pattern-info "host(some-domain.com); path(?user/status/?id); queryparam(offset=?offset); queryparam(list=?type); fragment(?paragraph)") 
                  "https://some-domain.com/some-username/status/1905065-Travel-Icons-pack?liiiiiist=users&offset=1&page=34#paragraph=3")))
    (is (= nil (recognize 
                  (pattern-info "host(some-domain.com); path(?user/status/?id); queryparam(offset=?offset); queryparam(list=?type); fragment(?paragraph)") 
                  "https://some-domain.com/some-username/status/1905065-Travel-Icons-pack?offset=1&page=34#paragraph=3")))
    (is (= nil (recognize 
                  (pattern-info "host(some-domain.com); path(?user/status/?id); queryparam(offset=?offset); queryparam(list=?type); fragment(?paragraph)") 
                  "https://some-domain.com/some-username/status/1905065-Travel-Icons-pack?list=users&offset=1&page=34")))
    (is (= nil (recognize 
                  (pattern-info "host(some-domain.com); path(?user/status/?id); queryparam(offset=?offset); queryparam(list=?type); fragment(?paragraph)") 
                  "https://some-domain.com/some-username/status/1905065-Travel-Icons-pack#paragraph=3")))
    (is (= nil (recognize 
                  (pattern-info "host(some-domain.com); path(?user/status/?id); queryparam(offset=?offset); queryparam(list=?type); fragment(?paragraph)") 
                  "https://some-domain.com/?list=users&offset=1&page=34#paragraph=3")))
    (is (= nil (recognize 
                  (pattern-info "host(some-domain.com); path(?user/status/?id); queryparam(offset=?offset); queryparam(list=?type); fragment(?paragraph)") 
                  "https://some-domain.com/some-username/status/1905065-Travel-Icons-pack")))
    (is (= nil (recognize 
                  (pattern-info "host(some-domain.com); path(?user/status/?id); queryparam(offset=?offset); queryparam(list=?type); fragment(?paragraph)") 
                  "https://some-domain.com/?list=users&offset=1&page=34")))
    (is (= nil (recognize 
                  (pattern-info "host(some-domain.com); path(?user/status/?id); queryparam(offset=?offset); queryparam(list=?type); fragment(?paragraph)") 
                  "https://some-domain.com/#paragraph=3")))
    (is (= nil (recognize 
                  (pattern-info "host(some-domain.com); path(?user/status/?id); queryparam(offset=?offset); queryparam(list=?type); fragment(?paragraph)") 
                  "https://some-domain.com/")))
    (is (= nil (recognize 
                  (pattern-info "host(some-domain.com); path(?user/status/?id); queryparam(offset=?offset); queryparam(list=?type); fragment(?paragraph)") 
                  "httttttttps://some-domain.com/some-username/status/1905065-Travel-Icons-pack?list=users&offset=1&page=34#paragraph=3")))
    (is (= nil (recognize 
                  (pattern-info "host(some-domain.com); path(?user/status/?id); queryparam(offset=?offset); queryparam(list=?type); fragment(?paragraph)") 
                  "://some-domain.com/some-username/status/1905065-Travel-Icons-pack?list=users&offset=1&page=34#paragraph=3")))
    (is (= nil (recognize 
                  (pattern-info "host(some-domain.com); path(?user/status/?id); queryparam(offset=?offset); queryparam(list=?type); fragment(?paragraph)") 
                  "some-domain.com/some-username/status/1905065-Travel-Icons-pack?list=users&offset=1&page=34#paragraph=3")))
  )
  (testing "URL-MATCH. Good uri."
    (let [inner-result (recognize 
                          (pattern-info "host(some-domain.com); path(?user/status/?id); queryparam(offset=?offset); queryparam(list=?type); fragment(?paragraph)") 
                          "https://some-domain.com/some-username/status/1905065-Travel-Icons-pack?list=users&offset=1&page=34#paragraph=3")]
      (is (= [[:user "some-username"] [:id "1905065-Travel-Icons-pack"] [:offset "1"] [:type "users"] [:paragraph "paragraph=3"]] inner-result)))
    (let [inner-result (recognize 
                          (pattern-info "host(); path(?user/status/?id); queryparam(offset=?offset); queryparam(list=?type); fragment(?paragraph)") 
                          "https://some-domain.com/some-username/status/1905065-Travel-Icons-pack?list=users&offset=1&page=34#paragraph=3")]
      (is (= [[:user "some-username"] [:id "1905065-Travel-Icons-pack"] [:offset "1"] [:type "users"] [:paragraph "paragraph=3"]] inner-result)))
    (let [inner-result (recognize 
                          (pattern-info "host(some-domain.com); queryparam(offset=?offset); queryparam(list=?type); fragment(?paragraph)") 
                          "https://some-domain.com/some-username/status/1905065-Travel-Icons-pack?list=users&offset=1&page=34#paragraph=3")]
      (is (= [[:offset "1"] [:type "users"] [:paragraph "paragraph=3"]] inner-result)))

    (let [inner-result (recognize 
                          (pattern-info "host(some-domain.com); path(?user/status/?id); queryparam(list=?type); fragment(?paragraph)") 
                          "https://some-domain.com/some-username/status/1905065-Travel-Icons-pack?list=users&offset=1&page=34#paragraph=3")]
      (is (= [[:user "some-username"] [:id "1905065-Travel-Icons-pack"] [:type "users"] [:paragraph "paragraph=3"]] inner-result)))
    (let [inner-result (recognize 
                          (pattern-info "host(some-domain.com); path(?user/status/?id); fragment(?paragraph)") 
                          "https://some-domain.com/some-username/status/1905065-Travel-Icons-pack?list=users&offset=1&page=34#paragraph=3")]
      (is (= [[:user "some-username"] [:id "1905065-Travel-Icons-pack"] [:paragraph "paragraph=3"]] inner-result)))
    (let [inner-result (recognize 
                          (pattern-info "host(some-domain.com); path(?user/status/?id); queryparam(offset=?offset); queryparam(list=?type);") 
                          "https://some-domain.com/some-username/status/1905065-Travel-Icons-pack?list=users&offset=1&page=34#paragraph=3")]
      (is (= [[:user "some-username"] [:id "1905065-Travel-Icons-pack"] [:offset "1"] [:type "users"]] inner-result)))
    (let [inner-result (recognize 
                          (pattern-info "host(some-domain.com);") 
                          "https://some-domain.com/some-username/status/1905065-Travel-Icons-pack?list=users&offset=1&page=34#paragraph=3")]
      (is (= [] inner-result)))
    (let [inner-result (recognize 
                          (pattern-info "host();") 
                          "https://some-domain.com/some-username/status/1905065-Travel-Icons-pack?list=users&offset=1&page=34#paragraph=3")]
      (is (= [] inner-result)))
  )
)
