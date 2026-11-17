# URL tracking scrubber: catalog and investigation

## Policy and evidence

Catalog snapshot: **2026-11-17**. `scrub(String)` accepts complete shared text, not just one URL. The Android share flow is unchanged. This is an independently curated static catalog, not a vendored cleaner database, a redirect resolver, or a guarantee of complete tracking removal.

The chosen policy removes known analytics, click IDs, recipient/sharer attribution, and **affiliate/referral-credit tags**. Losing referral credit, conversion matching, campaign attribution, or marketing prefill is intentional. Changing content identity, a selected seller/variation, playback state, routing, or access is not. A parameter's existence alone is not evidence of surveillance; campaign metadata need not identify an individual.

Evidence has different strengths:

- **Vendor semantics:** the primary sources below explain documented campaign/click/affiliate fields and functional fields. This establishes purpose, not universal safety on every application.
- **Community evidence:** [ClearURLs rules][clearurls], [AdGuard global rules][adguard-global], [AdGuard scoped rules][adguard-scoped], and [uBlock privacy rules][ublock] corroborate additional names and exceptions. In particular, opaque service-specific names such as YouTube/Spotify `si` and Instagram `igsh` are cleaner-supported, not a claim that those platforms publicly document their personalization algorithms. The extended global list likewise is not entirely vendor-documented.
- **Conservative scope:** content-path restrictions and preservation decisions below deliberately narrow upstream cleaners. Rule presence does not imply permission to copy all of a cleaner's behavior.

The implementation review retrieved Google's supported-domain snapshot, eBay's marketplace table, Apple's linking guide, AWS signing documentation, and the relevant AdGuard/uBlock rule excerpts on the snapshot date. Other primary links below are the approved catalog's evidence references; this change does not claim fresh end-to-end browser/mobile share captures or live destination tests for every product. Source links to `master` are mutable, not pinned revisions. Future changes should record a commit permalink/revision alongside the retrieval date. The fixtures use sanitized representative identifiers, not private share tokens or credentials.

ClearURLs/Rules is labeled LGPL-3.0 in [its repository metadata](https://github.com/ClearURLs/Rules). No external dataset is vendored here. Review upstream licenses before any future bulk dataset reuse; do not mistake a source citation for a license grant for copying a database.

## Parsing and preservation contract

- Treat whitespace according to Kotlin `Char.isWhitespace()`. Single-token shares take a direct path; for messages, replace only non-whitespace tokens using the equivalent Java character classes. Preserve every whitespace code unit, including tabs, CRLF, repeated spaces and NBSP, and all surrounding messages/Unicode.
- Recognize explicit case-insensitive HTTP(S), optionally inside matched outer `()`, `[]`, `{}`, `<>`, double quotes or single quotes. Set aside trailing sentence punctuation `. , ! ; :` for recognition only. Keep wrappers/punctuation in the output. Unmatched/ambiguous wrappers remain unchanged; balanced bare-URL path parentheses remain part of the URL. Do not extract links embedded in arbitrary words, Markdown labels, email addresses, or scheme-less domains.
- Fast-skip tokens without a query before the first fragment. Parse remaining candidates once with `java.net.URI`, catching `URISyntaxException`; require hierarchical HTTP(S) and a nonempty raw authority. A null parsed host, including an internationalized authority, allows globals only. Never classify userinfo or path text as a hostname.
- Normalize the parsed host with `Locale.ROOT` and remove one terminal DNS dot **for matching only**. Resolve the full host then successive label-delimited suffixes in a static hash map. The first registered root wins, even if its path is ineligible. Lookup cost depends on hostname labels, not catalog size. Choose a singleton family variant once before scanning names.
- Only the first literal `?` before the first literal `#` begins a query. Literal `&` separates entries; only the first literal `=` separates a name from its value. Do not treat `;`, `%26`, or `%3D` as separators or HTML-decode `&amp;`. Its literal `&` still separates entries. Decode names once for comparison; do not decode values or rewrite retained names. `%75tm_source` matches, `%2575tm_source` does not, and a literal `+` cannot match the catalog. Names are case-sensitive.
- Split the raw query at literal `&`, filter known names, and join the surviving raw entries. Kotlin's split preserves empty entries, including trailing ones. Retain unknown duplicates, valueless/empty-valued unknowns, ordering, scheme/host case, ports, userinfo, paths, percent-escape spelling and literal `+` versus `%20`. Remove `?` only when every entry is removed. Thus `?utm_source=x&` becomes `?` because the empty entry survives. This rebuilds the query from untouched strings, not decoded/re-encoded key/value pairs.
- An exact canonical name `X-Amz-Signature` or `X-Goog-Signature`, including once-percent-encoded names and empty/valueless instances, cancels **all** changes to that URL. These signatures cover other query fields; preserving only the signature itself is insufficient. This is a narrow safeguard, not detection of every signing scheme.
- Keep fragments/SPA routes and nested destination URLs untouched. Unsupported, malformed or signed URLs do not prevent cleanup of later whitespace tokens. Unchanged text remains equal to the input. Separate token/wrapper handling, URL validation, raw-entry filtering, and comparison-name decoding; favor these straightforward operations over primitive deletion buffers and output-offset bookkeeping.

[Java URI raw-accessor semantics](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/net/URI.html) and [MDN's URLSearchParams serialization examples](https://developer.mozilla.org/en-US/docs/Web/API/URLSearchParams) explain why parsing is useful but reserializing retained text is not lossless.

## Complete initial global catalog

These apply on unknown hosts too, subject to the signing safeguard. Remove every occurrence, including valueless/empty-valued entries. Names are exact unless a construction is explicitly stated.

| Group | Names / construction |
| --- | --- |
| UTM | Literal lowercase `utm_` prefix with at least one following character; not `utm` or `utm_` alone |
| Google ads, Merchant Center, cross-domain analytics | `gclid`, `dclid`, `gbraid`, `wbraid`, `gclsrc`, `gad_source`, `gad_campaignid`, `srsltid`, `_gl`, `_ga`; preserve `_ga` on exact `login.aliexpress.com` and `login.aliexpress.us` |
| Social/ad clicks | `fbclid`, `fbadid`, `msclkid`, `ttclid`, `twclid`, `li_fat_id`, `epik`, `ScCid`, `__twitter_impression` |
| Yandex/Openstat | `yclid`, `ysclid`, `ym_tracking_id`, `_openstat` |
| Matomo/Piwik | Static cross-product of prefixes `mtm_`, `matomo_`, `pk_`, `piwik_` and suffixes `campaign`, `source`, `keyword`, `medium`, `content`, `cid`, `group`, `placement`; also `pk_vid`. Not arbitrary `pk_*`/`piwik_*` |
| HubSpot | `__hsfp`, `__hssc`, `__hstc`, `_hsenc`, `_hsmi`, `hsCtaTracking`, `hsa_acc`, `hsa_ad`, `hsa_cam`, `hsa_grp`, `hsa_kw`, `hsa_la`, `hsa_mt`, `hsa_net`, `hsa_ol`, `hsa_src`, `hsa_tgt`, `hsa_ver` |
| Email/newsletters | `mc_cid`, `mc_eid`, `mkt_tok`, `ml_subscriber`, `ml_subscriber_hash`, `vero_id`, `vero_conv`, `oly_anon_id`, `rb_clickid`, `wickedid` |
| Adobe/Mapp/Coremetrics | `s_cid`, `adobe_mc_ref`, `adobe_mc_sdid`, `wt_mc`, `cm_mmc`, `cm_me`, `cm_cr`; not generic `cid` or `mc_*` |
| Affiliate networks | `awc`, `cjevent`, `cjdata`, `sscid`, `ir_campaignid`, `ir_adid`, `ir_partnerid`, `ranMID`, `ranEAID`, `ranSiteID` |
| AT Internet | `xtor`, `at_campaign`, `at_campaign_type`, `at_creation`, `at_emailtype`, `at_link`, `at_link_id`, `at_link_origin`, `at_link_type`, `at_medium`, `at_ptr_name`, `at_recipient_id`, `at_recipient_list`, `at_send_date` |
| Other explicit campaigns | `itm_source`, `itm_medium`, `itm_campaign`, `itm_content`, `itm_term`, `int_source`, `int_medium`, `int_campaign`, `int_content`, `int_term`, `Echobox` |

The broad UTM namespace is an intentional policy, not a universal compatibility claim. Upstream also records applications that misuse UTM-looking fields for functionality; the initial contract does not import that entire exception database.

## Complete domain-scoped catalog

Unless narrowed below, each registered root includes itself and label-delimited subdomains. `youtube.com.evil.example` and `notyoutube.com` do not match YouTube. Unlisted keys survive everywhere. These additional rules mostly have community rather than vendor semantics evidence [C][clearurls]/[A][adguard-scoped]/[U][ublock].

Coverage update (2026-11-17): a user observed `is` on `youtube.com`, so its existing short-link removal rule now also covers the main YouTube family. This scope extension is based on that user observation, not new vendor documentation of the token's internal meaning. Sign-in paths and recognized signed URLs remain protected; unrelated hosts and uppercase `IS` remain unchanged.

| Family | Roots and eligibility | Additional removals; preservation notes |
| --- | --- | --- |
| YouTube | `youtube.com`; exclude exact `/signin` and descendants | `si`, `feature`, `pp`, `is`. Keep `v`, `t`, `start`, `end`, `list`, `index`, `ab_channel`, playback/captions. Root lookup covers www/m/music |
| YouTubeShort | `youtu.be` | `si`, `feature`, `pp`, `is` (the latter explicitly listed by AdGuard). Keep video-ID path |
| Spotify | `spotify.com` | `si` |
| SpotifyOpen | Exact `open.spotify.com`; descendants fall back to ordinary Spotify set | `si`, `sp_cid`, `dlsi`, `pi`, `referral`. Keep path IDs, `context`, playback/time |
| Facebook | `facebook.com` | `mibextid`, `sfnsn`, `__tn__`, `comment_tracking`; also compiled `^__(?:cft\|xts)__\[[0-9]+\]$` (the `\|` here escapes a Markdown table pipe; the regex uses alternation). Keep `id`, `fbid`, `story_fbid`, `v`, `set`, `comment_id`; no broad `ref`/`hc_*` |
| Instagram | `instagram.com` | `igsh`, `igshid`; keep post/reel/story IDs |
| ThreadsLegacy | `threads.net` | `igshid`, from uBlock's explicit `domain=instagram.com\|threads.net` rule. Do not infer `threads.com` |
| Twitter | `twitter.com`; exclude `/i/redirect` and descendants | `s`, `t`, `src`, `ref_src`, `refsrc`, `ref_url`, `cn`. Here `t` is a share token, unlike YouTube's playback time |
| X | `x.com`; same redirect exclusion | Twitter set plus `a` |
| Reddit | `reddit.com`; only raw paths matching the Reddit pattern below | `share_id`, `ref_source`, `ref_campaign`, `correlation_id`, `rdt`, `_branch_match_id`, `entry_point`. Not opaque `/s/` shares or outbound redirect fields |
| TikTok | `tiktok.com`; only raw canonical video paths matching the pattern below | `_t`, `_r`, `_d`, `u_code`, `share_iid`, `share_link_id`, `share_app_name`, `share_region`, `social_share_type`, `tt_from`, `trackParams`, `ug_btm`, `is_from_webapp`, `sender_device`. Keep `share_app_id`, `timestamp`, generic `source` |
| LinkedIn | `linkedin.com` | `trk`, `trackingId`, `refId`. Keep `u`; no broad `li[a-z]{2}` |
| Snapchat | `snapchat.com` | `sc_referrer`, `sc_ua`; keep access/invite paths |
| Bilibili | `bilibili.com`; no family cleanup on `api.bilibili.com` or descendants; `space.bilibili.com` and descendants use only `spm_id_from` | Ordinary set: `spm_id_from`, `vd_source`, `share_source`, `share_medium`, `share_plat`, `share_tag`, `share_session_id`, `buvid`, `unique_k`. Keep `bvid`, `aid`, `cid`, `p`, `t`, `mid`, `timestamp` |
| Zhihu | `zhihu.com` | `share_code` |
| Medium | `medium.com` | `source`; keep access key `sk` |
| IMDb | `imdb.com` | `ref_`, `pf_rd_i`, `pf_rd_m`, `pf_rd_p`, `pf_rd_r`, `pf_rd_s`, `pf_rd_t` |
| Netflix | `netflix.com` | `trackId`, `tctx`; keep content IDs/timestamps |
| MSN | `msn.com` | `ocid`, `cvid` |
| Cnet | `cnet.com` | `ftag` |
| SteamStore | `steampowered.com`, not `steamcommunity.com` | `snr` |
| AppleMedia | `music.apple.com`, `tv.apple.com`; suffix lookup includes `geo.music.apple.com` | `at`, `ct`; keep `app`, `ls`, track `i`, region/storefront paths and playback |
| Amazon | Explicit regional roots below | `tag`, `ascsubtag`, `asc_campaign`, `asc_refurl`, `asc_source`, `linkCode`, `linkId`, `creative`, `creativeASIN`, `camp`, `AssociateTag`, `adgrpid`, `hvadid`, `hvbmt`, `hvdev`, `hvlocphy`, `hvnetw`, `hvqmt`, `hvrand`, `hvtargid`, `hydadcr`, `refRID`, `pd_rd_i`, `pd_rd_r`, `pd_rd_w`, `pd_rd_wg`, `pf_rd_i`, `pf_rd_m`, `pf_rd_p`, `pf_rd_r`, `pf_rd_s`, `pf_rd_t`, `pf_rd_w`. Keep `ref`, `ref_`, `smid`, `psc`, `th`, `sr`, `s`, `keywords`, product/variation/seller IDs and path `/ref=...` |
| Ebay | Explicit regional roots below; only `/itm` and descendants | `_trkparms`, `mkevt`, `mkcid`, `mkrid`, `campid`, `customid`, `toolid`, `sssrc`, `ssuid`, `ssspo`, `itmmeta`, `itmprp`, `amdata`. Keep `var`, `epid`, `hash`, `_trksid`, search/seller/variation fields |
| GoogleSearch | Google roots below; only root, `www.<root>` or `m.<root>`, exact `/search` | `ved`, `ei`, `oq`, `esrc`, `uact`, `aqs`, `rlz`, `source`, `sourceid`, `iflsig`, `cshid`, `gs_l`, `gs_lp`, `gs_lcp`, `gs_lcrp`, `sca_esv`, `sclient`, `fbs`, `sstk`. Keep `q`, `tbm`, `tbs`, language/filter fields |
| GoogleMaps | Root/www/m hosts on `/maps` and descendants; exact `maps.<root>` on any path | `source`, `g_ep`, `sca_esv`. Keep every Maps action/location key. No Google family cleanup on accounts/docs/drive/mail/news, other subdomains, or other paths |
| GoogleMapsShort | Exact `maps.app.goo.gl` only | `g_st`; never expand its opaque path |
| BingSearch | `bing.com`, exact `/search` only | `cvid`, `sk`, `sp`, `sc`, `qs`, `qp`, `form`; keep `q` and filters |

Raw path patterns, compiled once:

```text
Reddit: ^/(?:r/[^/]+/|user/[^/]+/)?comments/[^/]+(?:/.*)?$
TikTok: ^/@[^/]+/video/[0-9]+(?:/.*)?$
```

### Regional roots

These are explicit ownership snapshots, not arbitrary `amazon.*`, `ebay.*` or `google.*` matching. Nothing is fetched at application runtime.

Amazon, from the regional links on [Amazon's own Associates page](https://affiliate-program.amazon.com/help/operating/linking/):

```text
amazon.com amazon.co.uk amazon.com.au amazon.com.be amazon.eg amazon.ie
amazon.in amazon.pl amazon.sa amazon.se amazon.sg amazon.co.jp amazon.com.mx
amazon.es amazon.com.br amazon.ca amazon.com.tr amazon.fr amazon.de amazon.nl
amazon.it amazon.ae
```

eBay, from the [marketplace URL table](https://developer.ebay.com/api-docs/user-guides/static/mip-user-guide/mip-enum-marketplace.html). That table explicitly marks `ebay.in` nonfunctioning; many countries use paths on `ebay.com`, not new roots. `export.ebay.co.th` is covered by its listed root:

```text
ebay.com ebay.ca ebay.co.uk ebay.com.au ebay.at ebay.be ebay.fr ebay.de
ebay.it ebay.nl ebay.es ebay.ch ebay.com.hk ebay.ie ebay.ph ebay.pl
ebay.co.th ebay.vn ebay.co.jp
```

Google, with the leading dot removed from the [official supported-domains snapshot](https://www.google.com/supported_domains):

```text
google.com google.ad google.ae google.com.af google.com.ag google.al google.am
google.co.ao google.com.ar google.as google.at google.com.au google.az google.ba
google.com.bd google.be google.bf google.bg google.com.bh google.bi google.bj
google.com.bn google.com.bo google.com.br google.bs google.bt google.co.bw
google.by google.com.bz google.ca google.cd google.cf google.cg google.ch
google.ci google.co.ck google.cl google.cm google.cn google.com.co google.co.cr
google.com.cu google.cv google.com.cy google.cz google.de google.dj google.dk
google.dm google.com.do google.dz google.com.ec google.ee google.com.eg
google.es google.com.et google.fi google.com.fj google.fm google.fr google.ga
google.ge google.gg google.com.gh google.com.gi google.gl google.gm google.gr
google.com.gt google.gy google.com.hk google.hn google.hr google.ht google.hu
google.co.id google.ie google.co.il google.im google.co.in google.iq google.is
google.it google.je google.com.jm google.jo google.co.jp google.co.ke
google.com.kh google.ki google.kg google.co.kr google.com.kw google.kz
google.la google.com.lb google.li google.lk google.co.ls google.lt google.lu
google.lv google.com.ly google.co.ma google.md google.me google.mg google.mk
google.ml google.com.mm google.mn google.com.mt google.mu google.mv google.mw
google.com.mx google.com.my google.co.mz google.com.na google.com.ng
google.com.ni google.ne google.nl google.no google.com.np google.nr google.nu
google.co.nz google.com.om google.com.pa google.com.pe google.com.pg
google.com.ph google.com.pk google.pl google.pn google.com.pr google.ps
google.pt google.com.py google.com.qa google.ro google.ru google.rw
google.com.sa google.com.sb google.sc google.se google.com.sg google.sh
google.si google.sk google.com.sl google.sn google.so google.sm google.sr
google.st google.com.sv google.td google.tg google.co.th google.com.tj
google.tl google.tm google.tn google.to google.com.tr google.tt google.com.tw
google.co.tz google.com.ua google.co.ug google.co.uk google.com.uy
google.co.uz google.com.vc google.co.ve google.co.vi google.com.vn google.vu
google.ws google.rs google.co.za google.co.zm google.co.zw google.cat
```

## Reviewed but not enabled

- Do not adopt global `ref`, `source`, `spm`, `cmpid`, `cid`, whole `mc_*`/`pk_*` patterns, generic Branch deep-link fields, or Eloqua token families. Generic names can select content or route requests.
- Do not globally remove `irclickid` or `oly_enc_id`. AdGuard's global exclusions include `tix.axs.com` and `greenbuildingadvisor.com`, respectively. This offline function cannot infer those access/redirect contexts. Its `_ga` exclusions for the two exact AliExpress login hosts are retained explicitly.
- Keep TikTok `share_app_id`: AdGuard documents a 502 dependency when a referrer header is present and compensates with header removal. A shared-text scrubber cannot change request headers. See [the scoped rule source][adguard-scoped] and [issue 225183](https://github.com/AdguardTeam/AdguardFilters/issues/225183).
- Keep Amazon `ref`, `ref_`, `psc`, `smid`, and selection fields. Upstream disabled rules cite [ref breakage](https://github.com/AdguardTeam/AdguardFilters/issues/110571), [ref_ breakage](https://github.com/AdguardTeam/AdguardFilters/issues/88627), [psc](https://github.com/AdguardTeam/AdguardFilters/issues/157889#issuecomment-1660195416), and [chosen-seller/navigation breakage](https://github.com/AdguardTeam/AdguardFilters/issues/148958). Keep eBay `var` and `_trksid`: [AdGuard 208605](https://github.com/AdguardTeam/AdguardFilters/issues/208605), [uBlock 25247](https://github.com/uBlockOrigin/uAssets/issues/25247).
- Keep Bilibili `mid` and `timestamp` even though broader rules exist; [mid breakage discussion](https://github.com/AdguardTeam/AdguardFilters/issues/188783#issuecomment-2359649738) illustrates why source exceptions matter. API/space scopes are narrower than the ordinary family.
- Pinterest query `epik` is not cookie `_epik`. A cookie name is not automatically a query parameter. Preserve `_epik` unless separate evidence establishes a safe scoped rule.

### Global-only products and short links

No extra family rule is established here for Pinterest (external `epik` is global), Discord, Bluesky, Mastodon instances, Telegram, WhatsApp, SoundCloud, Vimeo, Deezer, Twitch, `threads.com`, or `youtube-nocookie.com`. Do not add empty/no-op family classes. Global analytics cleanup already works on federated instances without inventing a finite Mastodon registry.

Preserve Telegram `start`/invite payloads, WhatsApp `text`, Vimeo access/playback fields, and opaque shortener paths such as `t.co`, `spotify.link`, `pin.it`, `amzn.to`, `on.soundcloud.com`, `vm.tiktok.com`, and `vt.tiktok.com`. Their outer queries still receive global cleanup. In particular, the TikTok root's canonical-video eligibility does not infer removals on short-share paths.

## Primary evidence index

| Purpose | Vendor evidence and interpretation |
| --- | --- |
| Google campaigns and clicks | [Analytics UTM fields](https://support.google.com/analytics/answer/10917952?hl=en), [Ads click IDs](https://support.google.com/google-ads/answer/9744275?hl=en), [Merchant Center auto-tagging](https://support.google.com/merchants/answer/15191080?hl=en). Establish campaign/click attribution, not every extended spelling in community lists |
| Social/ad click IDs | [Meta](https://developers.facebook.com/documentation/ads-commerce/conversions-api/parameters/fbp-and-fbc), [Microsoft](https://learn.microsoft.com/en-us/advertising/campaign-management-service/accountproperty), [TikTok](https://ads.tiktok.com/gateway/docs/index?doc_id=1771100879787009&language=ENGLISH), [LinkedIn](https://learn.microsoft.com/en-us/linkedin/marketing/conversions/enabling-first-party-cookies) |
| Pinterest and Snap case/name distinctions | [Pinterest conversion API](https://developers.pinterest.com/docs/track-conversions/track-conversions-in-the-api/) defines query `epik`; [cookie documentation](https://help.pinterest.com/en/business/article/pinterest-tag-parameters-and-cookies) distinguishes `_epik`. [Snap API](https://developers.snap.com/marketing-api/Conversions-API/UsingTheAPI) documents mixed-case query `ScCid` |
| Campaign, email and prefill attribution | [Matomo aliases](https://matomo.org/faq/reports/advanced-how-to-manually-build-campaign-tracking-urls/), [HubSpot ad tracking](https://knowledge.hubspot.com/ads/ad-tracking-in-hubspot), [Mailchimp campaign/customer identifiers](https://mailchimp.com/developer/marketing/concepts/e-commerce/), [Marketo tracking/prefill](https://experienceleague.adobe.com/en/docs/marketo/using/product-docs/email-marketing/general/functions-in-the-editor/disable-tracking-for-an-email-link) |
| Affiliate credit | [Amazon linking](https://affiliate-program.amazon.com/help/operating/linking/) establishes affiliate tags. [Apple linking](https://performance-partners.apple.com/linking) explicitly distinguishes affiliate `at`, campaign `ct`, app selection `app`, and `ls`. Preserve track-selection `i` and paths |
| Functional playback/location fields | [YouTube time-aware sharing](https://support.google.com/youtube/answer/57741?co=GENIE.Platform%3DAndroid&hl=en), [Maps URL actions and locations](https://developers.google.com/maps/documentation/urls/get-started). Time, playlist, destination and place identifiers are not blanket tracking fields |
| Signed URLs | [AWS signed query requests](https://docs.aws.amazon.com/AmazonS3/latest/developerguide/sigv4-query-string-auth.html), [Google Cloud canonical requests](https://docs.cloud.google.com/storage/docs/authentication/canonical-requests?hl=en). Query changes can invalidate access signatures |

## Repeatable investigation and extension process

1. **Inventory owned surfaces.** Find canonical web/mobile/music/embed/legacy/short-share hosts and actual path shapes from product documentation and public share links. Establish ownership explicitly; do not generate arbitrary country-TLD suffixes. For federation, rely on global cleanup unless a genuinely safe host-independent rule is established.
2. **Capture sanitized examples.** Compare browser and mobile share actions, logged in/out where available. Record retrieval date, platform/version, canonical content ID, query names and which fields vary between independent shares. Never publish real sharer IDs, access tokens, or presigned credentials. Do not claim a synthetic fixture is an observed share capture.
3. **Establish semantics.** Prefer vendor documentation or SDK source. Record exact spelling/case, host/path scope, evidence URL and revision, and classify click ID, campaign, recipient/sharer attribution, referral credit, content selection, access, or routing. Parameter presence alone is not proof of surveillance.
4. **Cross-check maintained cleaners.** Inspect ClearURLs, AdGuard and uBlock rules **with exceptions, disabled rules and linked issues**. Record cleaner-only evidence honestly. Distinguish URL names from cookies (`epik` versus `_epik`). Do not port URL rewriting, redirect expansion or broad generic-name policies.
5. **Test one field at a time.** Use a disposable browser session and compare destination/canonical ID, content/variation/seller, playlist/time, invite/access and redirects. Network inspection is an investigator tool, never part of `scrub()`. Do not request private links merely to expand them. If evidence is absent/conflicting or behavior changes, retain the parameter and record the gap rather than broaden a regex.
6. **Integrate narrowly.** Update the exact singleton set and aliases/eligibility in `domains/UrlFamily.kt` and `domains/TrackingCatalog.kt`. Add a real sanitized input→expected-output fixture and an important preserve/false-positive fixture in `ScrubTest.kt`. Check host boundaries, percent escapes, duplicates and relevant signing/access cases. Run the focused command below. Global sets, maps and regexes are constructed once; do not add per-URL rules objects or a catalog-wide dispatch scan.
7. **Refresh intentionally.** Revisit sources after a new share format/domain, reported breakage, or deliberate catalog refresh. Preserve source dates/revisions and explain additions/removals. A finite static list cannot cover all current or future attribution.

Suggested evidence record for an addition: retrieval date; platform/share action and login context; source URL/commit; owned host/raw path; exact name/case; purpose and confidence; sanitized observed URL; one-field comparison outcome; preserved content/access fields; accepted/rejected decision and reason; regression fixture name.

Focused local verification from the repository root, with configured Android SDK and Java:

```sh
./gradlew :app:testDebugUnitTest --tests com.github.kermetian.ribbit.ScrubTest --console=plain
```

The tests invoke the same public pure-JVM function as the activity, with explicit golden strings and idempotence. They require no Android URI APIs, emulator, instrumentation, network access or new dependency. They establish local text behavior, not ongoing remote-site compatibility.

## Simplicity and runtime tradeoff

The expected share is a **single URL**. `scrub` sends that directly to token cleanup; full-message support remains available without changing whitespace. The implementation separates wrapper handling, URL validation/filtering and comparison-name decoding. The tracker catalog and host/path dispatch are unchanged. This replaces the original deletion-span buffer, buffer growth, pending-run state and output-offset bookkeeping with Kotlin `split`, `filter` and `joinToString` over raw query strings. `Scrub.kt` goes from 190 to 106 logical lines. More temporary strings/lists are intentional; URI components are still not re-encoded.

The comparison uses the original compiled implementation saved before the edit and the real Gradle-compiled replacement, not a surrogate algorithm. Environment: Apple M5 arm64, Temurin OpenJDK 21.0.11, Kotlin stdlib 2.2.10, `-Xms256m -Xmx256m` with the JVM's default collector.

Measurement method:

- A disposable Java harness invokes the public `ScrubKt.scrub` directly. Each scenario cycles through 128 prebuilt sanitized inputs, varying content/share IDs. Inputs are prepared outside the timed loop.
- Three independent JVM forks per implementation, alternating which implementation runs first. Each scenario gets five 200 ms warmup rounds followed by seven 150 ms measured rounds per fork; the results summarize 21 measured rounds. Timing is batched in 256-call groups with `System.nanoTime`; every returned string escapes through the same volatile sink to prevent dead-code elimination.
- Allocation is measured with `com.sun.management.ThreadMXBean.getThreadAllocatedBytes`, divided by calls. This counts temporary allocations, not retained heap or application memory usage.
- Single-social mixes YouTube, Spotify, Instagram and X shares. Single-mixed adds Amazon, Google Search, Reddit, TikTok, global analytics and percent-encoded names. Separate workloads cover queryless URLs, preserved queries, signed URLs, and a message with three URLs, wrappers, tabs, CRLF and NBSP.
- Separately, 15 fresh JVMs per implementation time only the **first scrub call**, including scrubber/Kotlin/catalog class initialization but excluding JVM process startup. That is more relevant to a one-off share than steady-state throughput alone.

This is an observational desktop-JVM microbenchmark, not JMH, Android ART, an end-to-end share-activity measurement, or a latency guarantee. Warmup, JIT, GC and the host affect results. Small differences should not drive extra complexity; compare their absolute cost for one share, and distinguish cold initialization from warmed calls.

Measured on 2026-11-17. Warmed medians across the 21 measured rounds; time is per complete `scrub` call, allocation is bytes per call:

| Workload | Original time (µs) | Simplified time (µs) | Original allocation (B) | Simplified allocation (B) |
| --- | ---: | ---: | ---: | ---: |
| Single social/media URL | 0.613 | 0.579 | 848 | 1,562 |
| Single mixed-domain URL | 0.842 | 0.781 | 1,091 | 2,017 |
| Single URL without query | 0.073 | 0.048 | 0 | 0 |
| Single URL with preserved query | 0.657 | 0.597 | 664 | 1,192 |
| Single signed URL | 0.702 | 0.582 | 592 | 1,098 |
| Message containing three URLs | 1.496 | 1.922 | 2,368 | 7,664 |

Cold first-call results, 15 fresh JVMs per implementation:

| Implementation | Median first call (ms) | Observed range (ms) | First-call thread allocation (B) |
| --- | ---: | ---: | ---: |
| Original | 21.224 | 20.850–22.714 | 4,853,664 |
| Simplified | 25.815 | 25.303–26.203 | 5,228,712 |

Interpretation: on this JVM, simplifying did not make warmed single-URL calls slower despite allocating approximately 0.5–0.9 KB more per queried URL. The three-URL message costs about 0.43 µs and 5.3 KB more. Cold first-call initialization is measurably worse by about 4.6 ms and 375 KB, so it would be misleading to report only warmed throughput. These cold figures include runtime/class/catalog initialization and do not isolate the cost of string operations.

For occasional single-URL sharing, these measurements favor the simpler implementation over allocation bookkeeping; they do not establish Android latency or justify further micro-optimizations. All **137** exact-string/idempotence regression cases passed with the focused Gradle command after the refactor, including additional literal-plus and non-ASCII comparison-name preservation cases. The Android share activity and catalog were not changed. No benchmark dependency or permanent harness was added to the app.

## Limitations

Tracking embedded in opaque content/shortener IDs cannot be removed offline. Fragments and nested URLs are intentionally untouched. Cleanup does not eliminate cookies, fingerprinting, IP-address tracking or server-side attribution. Some applications misuse campaign-looking names for application behavior; unknown signing schemes are not protected automatically. Broader cleanup is not automatically safer. Preserve unknown fields and investigate reported breakage rather than silently inventing new matching or redirect behavior.

[clearurls]: https://github.com/ClearURLs/Rules/blob/master/data.min.json
[adguard-global]: https://github.com/AdguardTeam/AdguardFilters/blob/master/TrackParamFilter/sections/general_url.txt
[adguard-scoped]: https://github.com/AdguardTeam/AdguardFilters/blob/master/TrackParamFilter/sections/specific.txt
[ublock]: https://github.com/uBlockOrigin/uAssets/blob/master/filters/privacy.txt
