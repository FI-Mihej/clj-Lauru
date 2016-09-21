# Introduction to clj-lauru

CLJ-Lauru is a Clojure library for easy parsing and matching URLs.

## Usage

### Let's say 

we have next kind of URLs:
`"http://some-domain.com/<user-id>/friend/<friend-id>?last-book-id=<last-book-id>&last-page=<last-page>#<paragraph>"`

For example this URL:
`"http://some-domain.com/2345/friend/5467?last-book-id=se56df&last-page=42#as12"`

### So we should create parsing pattern for it.

If we want to get whole info - we can use this pattern:

```clojure
=>(def pattern-1
      (pattern-info "host(some-domain.com); path(?user/friend/?friend); queryparam(last-book-id=?last-book); queryparam(last-page=?last-page); fragment(?paragraph)"))
```

If we want to get only some part - we can use, for example, that pattern (or any appropriate):

```clojure
=>(def pattern-2
      (pattern-info "host(some-domain.com); queryparam(last-book-id=?last-book); fragment(?paragraph)"))
```

If we want to apply this pattern to any domain - we can use empty 'host()' part:

```clojure
=>(def pattern-3
      (pattern-info "host(); queryparam(last-book-id=?last-book); fragment(?paragraph)"))
```

#### General rules:

1. "host(...)" section is always required (and can be empty for "any domain"). Any other sections may be omitted but cannot be empty.
2. Word prefixed with "?" will be converted to `keyword`. So make sure it is an appropriate name for a Clojure language naming rules.
3. Only `http` and `https` currently supported. It will change in next versions.

### Next

We'll just call `(recognize )` function for interesting URL:

```clojure
=>(recognize pattern-1 "http://some-domain.com/2345/friend/5467?last-book-id=se56df&last-page=42#as12")
```

And it will return this:

```clojure
[ [:user "2345"] 
  [:friend "5467"] 
  [:last-book "se56df"] 
  [:last-page "42"] 
  [:paragraph "as12"]]
```

That's it.

### Few more "good" examples

For pattern-2:

```clojure
=>(recognize pattern-2 "https://some-domain.com/2345/friend/5467?last-book-id=se56df&last-page=42#as12")

[ [:last-book "se56df"] 
  [:paragraph "as12"]]
```

For pattern-2 and `"https://some-domain.com/?last-book-id=se56df#as12"`:

```clojure
=>(recognize pattern-2 "https://some-domain.com/?last-book-id=se56df#as12")

[ [:last-book "se56df"] 
  [:paragraph "as12"]]
```

For pattern-3 and `"https://ANOTHER-DOMAIN.ORG/2345/friend/5467?last-book-id=se56df&last-page=42#as12"`:

```clojure
=>(recognize pattern-3 "https://ANOTHER-DOMAIN.ORG/2345/friend/5467?last-book-id=se56df&last-page=42#as12")

[ [:last-book "se56df"] 
  [:paragraph "as12"]]
```

### Also (for "bad" URLs)

If URL did not match pattern - `(recognize )` function will return `nil`:

```clojure
; There is no required info in url ('path', one of the 'queryparam' and 'fragment'):
=>(recognize pattern-1 "https://some-domain.com/?last-book-id=se56df#as12")

; There is wrong domain for this pattern:
=>(recognize pattern-2 "https://ANOTHER-DOMAIN.ORG/?last-book-id=se56df#as12")

; There is only '<user-id>/friend' ('2345/friend') in URL when pattern require '<user-id>/friend/<friend-id>': 
=>(recognize pattern-1 "http://some-domain.com/2345/friend?last-book-id=se56df&last-page=42#as12")

; etc.
```

### More examples

can be found in "test/core_test/core_test.clj"

