package com.github.kermetian.ribbit

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class ScrubTest(
    private val caseName: String,
    private val input: String,
    private val expected: String,
) {
    @Test
    fun scrubsTrackingWithoutChangingRetainedText() {
        val actual = scrub(input)
        assertEquals(caseName, expected, actual)
        assertEquals("$caseName remains unchanged on a second pass", actual, scrub(actual))
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun cases(): List<Array<String>> = listOf(
            arrayOf(
                "message and queryless URL",
                "hey check out this song! https://example.org",
                "hey check out this song! https://example.org",
            ),
            arrayOf(
                "music YouTube preserves message whitespace and playback",
                "  hey\tcheck\r\nhttps://music.youtube.com/watch?v=V&si=S&list=L&t=12#lyrics  ",
                "  hey\tcheck\r\nhttps://music.youtube.com/watch?v=V&list=L&t=12#lyrics  ",
            ),
            arrayOf(
                "YouTube short links remove both share identifiers",
                "https://youtu.be/V?si=A&is=B&t=30",
                "https://youtu.be/V?t=30",
            ),
            arrayOf(
                "YouTube removes duplicate and consecutive trackers",
                "https://www.youtube.com/watch?si=A&v=V&si=B&feature=shared&pp=C",
                "https://www.youtube.com/watch?v=V",
            ),
            arrayOf(
                "YouTube removes lowercase is but preserves uppercase IS and playback",
                "https://youtube.com/watch?is=SHARE&v=V&t=30&list=L&IS=KEEP&start=1&end=90",
                "https://youtube.com/watch?v=V&t=30&list=L&IS=KEEP&start=1&end=90",
            ),
            arrayOf(
                "www YouTube removes duplicate and valueless is without changing playback",
                "https://www.youtube.com/watch?v=V&is=SHARE&t=30&list=L&is&index=2&cc_load_policy=1",
                "https://www.youtube.com/watch?v=V&t=30&list=L&index=2&cc_load_policy=1",
            ),
            arrayOf(
                "music YouTube removes is while preserving playlist playback and fragment",
                "https://music.youtube.com/watch?v=V&t=12&list=L&index=2&is=SHARE#lyrics",
                "https://music.youtube.com/watch?v=V&t=12&list=L&index=2#lyrics",
            ),
            arrayOf(
                "is remains on unrelated hosts including YouTube hostname lookalikes",
                "https://example.org/watch?is=KEEP&v=V&utm_source=M https://youtube.com.evil.example/watch?is=KEEP&v=V&utm_source=M",
                "https://example.org/watch?is=KEEP&v=V https://youtube.com.evil.example/watch?is=KEEP&v=V",
            ),
            arrayOf(
                "Spotify open retains playlist context",
                "https://open.spotify.com/track/T?si=A&dlsi=B&pi=C&context=playlist%3AP",
                "https://open.spotify.com/track/T?context=playlist%3AP",
            ),
            arrayOf(
                "Facebook retains photo and collection IDs",
                "https://www.facebook.com/photo.php?fbid=P&mibextid=M&set=S",
                "https://www.facebook.com/photo.php?fbid=P&set=S",
            ),
            arrayOf(
                "Instagram removes entire tracking-only query",
                "https://www.instagram.com/reel/R/?igsh=X&igshid=Y",
                "https://www.instagram.com/reel/R/",
            ),
            arrayOf(
                "Reddit canonical post retains sort",
                "https://www.reddit.com/r/music/comments/P/song/?share_id=S&ref_source=share&rdt=1&sort=top",
                "https://www.reddit.com/r/music/comments/P/song/?sort=top",
            ),
            arrayOf(
                "Reddit opaque share path receives only global cleanup",
                "https://www.reddit.com/r/music/s/opaque?share_id=S&utm_source=share",
                "https://www.reddit.com/r/music/s/opaque?share_id=S",
            ),
            arrayOf(
                "X share token is not playback time",
                "https://x.com/u/status/123?t=T&s=20&a=A",
                "https://x.com/u/status/123",
            ),
            arrayOf(
                "TikTok video retains functional share app and timestamp",
                "https://www.tiktok.com/@u/video/123?_t=T&share_link_id=L&share_app_id=123&timestamp=12",
                "https://www.tiktok.com/@u/video/123?share_app_id=123&timestamp=12",
            ),
            arrayOf(
                "unknown host receives global click ID cleanup",
                "https://example.org/a?utm_source=mail&gclid=G&fbclid=F&epik=E&ScCid=S&li_fat_id=L&id=7",
                "https://example.org/a?id=7",
            ),
            arrayOf(
                "campaign aliases do not broaden to arbitrary prefixes",
                "https://example.org/?pk_campaign=C&matomo_keyword=K&mc_eid=E&_hsenc=H&mkt_tok=M&pk_key=PUBLIC&mc_custom=X",
                "https://example.org/?pk_key=PUBLIC&mc_custom=X",
            ),
            arrayOf(
                "Apple music retains song app and store selection",
                "https://music.apple.com/us/album/A?i=T&at=AFF&ct=MAIL&app=music&ls=1",
                "https://music.apple.com/us/album/A?i=T&app=music&ls=1",
            ),
            arrayOf(
                "Amazon UK retains seller and variation",
                "https://www.amazon.co.uk/dp/ASIN?tag=affiliate-21&linkCode=abc&smid=SELLER&psc=1&th=1",
                "https://www.amazon.co.uk/dp/ASIN?smid=SELLER&psc=1&th=1",
            ),
            arrayOf(
                "eBay item retains variation and functional tracking-like key",
                "https://www.ebay.de/itm/123?mkcid=1&campid=2&var=456&_trksid=keep",
                "https://www.ebay.de/itm/123?var=456&_trksid=keep",
            ),
            arrayOf(
                "Google Japan search retains query and image mode",
                "https://www.google.co.jp/search?q=coffee&ved=V&sourceid=chrome&tbm=isch",
                "https://www.google.co.jp/search?q=coffee&tbm=isch",
            ),
            arrayOf(
                "Google Maps retains route and travel mode",
                "https://www.google.com/maps/dir/?api=1&origin=A&destination=B&travelmode=walking&g_ep=G",
                "https://www.google.com/maps/dir/?api=1&origin=A&destination=B&travelmode=walking",
            ),
            arrayOf(
                "matching normalizes host only and never reserializes survivors",
                "HTTPS://Music.YouTube.Com.:443/watch?v=A%2fb&si=X&x=a+b&x=a%20b&flag&blank=#Z",
                "HTTPS://Music.YouTube.Com.:443/watch?v=A%2fb&x=a+b&x=a%20b&flag&blank=#Z",
            ),
            arrayOf(
                "query names decode exactly once and remain case sensitive",
                "https://example.org/?%75tm_source=X&%2575tm_source=Y&GCLID=Z&keep=%2f",
                "https://example.org/?%2575tm_source=Y&GCLID=Z&keep=%2f",
            ),
            arrayOf(
                "retained empty entries are not normalized away",
                "https://example.org/?a=1&&utm_source=X&&b=2&",
                "https://example.org/?a=1&&&b=2&",
            ),
            arrayOf(
                "trailing tracker run removes its preceding separator",
                "https://example.org/?a=1&utm_source=X&utm_medium=Y#f",
                "https://example.org/?a=1#f",
            ),
            arrayOf(
                "trailing empty entry keeps question mark",
                "https://example.org/?utm_source=X&",
                "https://example.org/?",
            ),
            arrayOf(
                "wrappers and sentence punctuation surround cleaned URLs",
                "look (https://youtu.be/V?si=X). <https://example.org/?utm_source=mail>",
                "look (https://youtu.be/V). <https://example.org/>",
            ),
            arrayOf(
                "domain embedded in a hostile hostname is not a family",
                "https://youtube.com.evil.example/watch?v=V&si=S&utm_source=X",
                "https://youtube.com.evil.example/watch?v=V&si=S",
            ),
            arrayOf(
                "userinfo cannot select a family",
                "https://youtube.com@evil.example/watch?si=S",
                "https://youtube.com@evil.example/watch?si=S",
            ),
            arrayOf(
                "generic names and fragment tracking text survive",
                "https://example.org/?id=1&ref=R&source=S&si=I&s=2&t=3#utm_source=fragment",
                "https://example.org/?id=1&ref=R&source=S&si=I&s=2&t=3#utm_source=fragment",
            ),
            arrayOf(
                "AWS signature vetoes earlier deletions",
                "https://storage.example/a?utm_source=X&X-Amz-Signature=S&id=1",
                "https://storage.example/a?utm_source=X&X-Amz-Signature=S&id=1",
            ),
            arrayOf(
                "LinkedIn keeps outbound destination and unknown li names",
                "https://www.linkedin.com/feed/update/URN?trk=T&trackingId=I&refId=R&u=https%3A%2F%2Fexample.org&like=1",
                "https://www.linkedin.com/feed/update/URN?u=https%3A%2F%2Fexample.org&like=1",
            ),
            arrayOf(
                "Snapchat retains invite path and access field",
                "https://www.snapchat.com/invite/TOKEN?sc_referrer=R&sc_ua=U&invite=ACCESS",
                "https://www.snapchat.com/invite/TOKEN?invite=ACCESS",
            ),
            arrayOf(
                "Bilibili video retains content page and playback identifiers",
                "https://www.bilibili.com/video/BV1?spm_id_from=S&vd_source=V&share_source=S&share_medium=M&share_plat=P&share_tag=T&share_session_id=I&buvid=B&unique_k=K&bvid=BV1&aid=1&cid=2&p=3&t=4&mid=5&timestamp=6",
                "https://www.bilibili.com/video/BV1?bvid=BV1&aid=1&cid=2&p=3&t=4&mid=5&timestamp=6",
            ),
            arrayOf(
                "Bilibili API rejects family rules but still removes global analytics",
                "https://api.bilibili.com/x?spm_id_from=S&vd_source=V&cid=2&utm_source=M",
                "https://api.bilibili.com/x?spm_id_from=S&vd_source=V&cid=2",
            ),
            arrayOf(
                "Bilibili API exclusion covers descendants",
                "https://edge.api.bilibili.com/x?share_source=S&spm_id_from=P&gclid=G",
                "https://edge.api.bilibili.com/x?share_source=S&spm_id_from=P",
            ),
            arrayOf(
                "Bilibili space narrows removals to one field",
                "https://space.bilibili.com/123?spm_id_from=S&vd_source=V&share_source=R&mid=123",
                "https://space.bilibili.com/123?vd_source=V&share_source=R&mid=123",
            ),
            arrayOf(
                "Bilibili space variant covers descendants",
                "https://m.space.bilibili.com/123?spm_id_from=S&unique_k=K&mid=123",
                "https://m.space.bilibili.com/123?unique_k=K&mid=123",
            ),
            arrayOf(
                "Bilibili exclusions require label boundaries",
                "https://notapi.bilibili.com/video/BV1?spm_id_from=S&vd_source=V&cid=2",
                "https://notapi.bilibili.com/video/BV1?cid=2",
            ),
            arrayOf(
                "Zhihu retains answer and content selection",
                "https://www.zhihu.com/question/123/answer/456?share_code=S&answer_id=456",
                "https://www.zhihu.com/question/123/answer/456?answer_id=456",
            ),
            arrayOf(
                "Medium retains friend-link access token",
                "https://medium.com/@u/article-ID?source=share&sk=ACCESS&mode=read",
                "https://medium.com/@u/article-ID?sk=ACCESS&mode=read",
            ),
            arrayOf(
                "IMDb retains title and language",
                "https://www.imdb.com/title/tt123/?ref_=R&pf_rd_i=I&pf_rd_m=M&pf_rd_p=P&pf_rd_r=R&pf_rd_s=S&pf_rd_t=T&language=fr",
                "https://www.imdb.com/title/tt123/?language=fr",
            ),
            arrayOf(
                "Netflix preserves title and timestamp",
                "https://www.netflix.com/watch/123?trackId=T&tctx=C&t=90",
                "https://www.netflix.com/watch/123?t=90",
            ),
            arrayOf(
                "MSN retains article and content selection",
                "https://www.msn.com/en-us/news/article/ID?ocid=O&cvid=C&id=ARTICLE",
                "https://www.msn.com/en-us/news/article/ID?id=ARTICLE",
            ),
            arrayOf(
                "Cnet retains article pagination",
                "https://www.cnet.com/tech/article/?ftag=F&page=2",
                "https://www.cnet.com/tech/article/?page=2",
            ),
            arrayOf(
                "Steam store retains app and language",
                "https://store.steampowered.com/app/123/Title/?snr=S&l=french",
                "https://store.steampowered.com/app/123/Title/?l=french",
            ),
            arrayOf(
                "Steam community does not inherit store rules",
                "https://steamcommunity.com/sharedfiles/filedetails/?id=123&snr=S&utm_source=M",
                "https://steamcommunity.com/sharedfiles/filedetails/?id=123&snr=S",
            ),
            arrayOf(
                "Bing search retains query and page offset",
                "https://www.bing.com/search?q=coffee&cvid=C&sk=K&sp=P&sc=C&qs=Q&qp=P&form=F&first=11",
                "https://www.bing.com/search?q=coffee&first=11",
            ),
            arrayOf(
                "Bing non-search paths retain family-looking fields",
                "https://www.bing.com/search/images?q=coffee&form=F&cvid=C&utm_source=M",
                "https://www.bing.com/search/images?q=coffee&form=F&cvid=C",
            ),
            arrayOf(
                "Maps short link keeps opaque ID and action",
                "https://maps.app.goo.gl/OPAQUE?g_st=share&api=1",
                "https://maps.app.goo.gl/OPAQUE?api=1",
            ),
            arrayOf(
                "Maps short rule is exact-host only",
                "https://m.maps.app.goo.gl/OPAQUE?g_st=share&utm_source=M",
                "https://m.maps.app.goo.gl/OPAQUE?g_st=share",
            ),
            arrayOf(
                "legacy Threads retains post and unrelated igsh",
                "https://www.threads.net/@u/post/ID?igshid=S&igsh=KEEP&reply=123",
                "https://www.threads.net/@u/post/ID?igsh=KEEP&reply=123",
            ),
            arrayOf(
                "Threads com does not inherit legacy rules",
                "https://www.threads.com/@u/post/ID?igshid=S&utm_source=M",
                "https://www.threads.com/@u/post/ID?igshid=S",
            ),
            arrayOf(
                "ordinary Spotify removes only si",
                "https://play.spotify.com/track/T?si=S&sp_cid=C&dlsi=D&pi=P&referral=R&position=30",
                "https://play.spotify.com/track/T?sp_cid=C&dlsi=D&pi=P&referral=R&position=30",
            ),
            arrayOf(
                "exact open Spotify removes additional attribution",
                "https://open.spotify.com/track/T?si=S&sp_cid=C&dlsi=D&pi=P&referral=R&t=30",
                "https://open.spotify.com/track/T?t=30",
            ),
            arrayOf(
                "open Spotify descendant falls back to ordinary Spotify",
                "https://m.open.spotify.com/track/T?si=S&sp_cid=C&dlsi=D&pi=P&referral=R",
                "https://m.open.spotify.com/track/T?sp_cid=C&dlsi=D&pi=P&referral=R",
            ),
            arrayOf(
                "Facebook indexed tracking names decode without widening regex",
                "https://m.facebook.com/story.php?id=1&story_fbid=2&v=3&comment_id=4&sfnsn=S&__tn__=T&comment_tracking=C&__cft__%5B0%5D=X&__xts__[123]=Y&__cft__[]=KEEP&__xts__[-1]=KEEP&__cft__[x]=KEEP&x__cft__[1]=KEEP&__cft__[1]x=KEEP&ref=R&hc_ref=H",
                "https://m.facebook.com/story.php?id=1&story_fbid=2&v=3&comment_id=4&__cft__[]=KEEP&__xts__[-1]=KEEP&__cft__[x]=KEEP&x__cft__[1]=KEEP&__cft__[1]x=KEEP&ref=R&hc_ref=H",
            ),
            arrayOf(
                "Twitter retains X-only a and status identity",
                "https://mobile.twitter.com/u/status/123?s=S&t=T&src=S&ref_src=R&refsrc=R&ref_url=R&cn=C&a=KEEP&id=123",
                "https://mobile.twitter.com/u/status/123?a=KEEP&id=123",
            ),
            arrayOf(
                "Twitter redirect exclusion retains routing parameters",
                "https://twitter.com/i/redirect?ref_url=DEST&t=T&utm_source=M https://x.com/i/redirect/next?a=A&src=S&fbclid=F",
                "https://twitter.com/i/redirect?ref_url=DEST&t=T https://x.com/i/redirect/next?a=A&src=S",
            ),
            arrayOf(
                "Twitter redirect exclusion is a path boundary",
                "https://x.com/i/redirected?a=A&t=T&id=123",
                "https://x.com/i/redirected?id=123",
            ),
            arrayOf(
                "YouTube sign-in exclusions preserve family fields",
                "https://youtube.com/signin?si=S&is=KEEP&feature=F&utm_source=M https://youtube.com/signin/next?pp=P&is=KEEP&fbclid=F",
                "https://youtube.com/signin?si=S&is=KEEP&feature=F https://youtube.com/signin/next?pp=P&is=KEEP",
            ),
            arrayOf(
                "YouTube sign-in boundary does not exclude similar paths",
                "https://m.youtube.com/signing?si=S&is=SHARE&v=V&start=1&end=9&index=2&ab_channel=C&cc_load_policy=1",
                "https://m.youtube.com/signing?v=V&start=1&end=9&index=2&ab_channel=C&cc_load_policy=1",
            ),
            arrayOf(
                "Reddit root comments accepts a post without title suffix",
                "https://reddit.com/comments/P?ref_campaign=R&correlation_id=C&_branch_match_id=B&entry_point=E&comment=123",
                "https://reddit.com/comments/P?comment=123",
            ),
            arrayOf(
                "Reddit user comments accepts descendants",
                "https://reddit.com/user/alice/comments/P/title/comment?share_id=S&sort=new",
                "https://reddit.com/user/alice/comments/P/title/comment?sort=new",
            ),
            arrayOf(
                "Reddit rejects incomplete or unrelated comment paths",
                "https://reddit.com/comments/?share_id=S&utm_source=M https://reddit.com/r/music/comments?rdt=1&gclid=G https://reddit.com/x/comments/P?entry_point=E&fbclid=F",
                "https://reddit.com/comments/?share_id=S https://reddit.com/r/music/comments?rdt=1 https://reddit.com/x/comments/P?entry_point=E",
            ),
            arrayOf(
                "TikTok canonical video removes exact share catalog only",
                "https://m.tiktok.com/@alice/video/123/extra?_r=R&_d=D&u_code=U&share_iid=I&share_app_name=N&share_region=R&social_share_type=T&tt_from=F&trackParams=P&ug_btm=B&is_from_webapp=W&sender_device=D&source=KEEP&share_app_id=123&timestamp=12",
                "https://m.tiktok.com/@alice/video/123/extra?source=KEEP&share_app_id=123&timestamp=12",
            ),
            arrayOf(
                "TikTok path guards reject non-video and non-numeric IDs",
                "https://tiktok.com/@u?_t=T&utm_source=M https://tiktok.com/@u/video/abc?_r=R&gclid=G https://tiktok.com/@u/video/123x?share_link_id=L&fbclid=F https://tiktok.com/video/123?_t=T&utm_source=M",
                "https://tiktok.com/@u?_t=T https://tiktok.com/@u/video/abc?_r=R https://tiktok.com/@u/video/123x?share_link_id=L https://tiktok.com/video/123?_t=T",
            ),
            arrayOf(
                "Apple TV and geo music share affiliate policy",
                "https://tv.apple.com/us/movie/ID?at=A&ct=C&playback=resume https://geo.music.apple.com/gb/album/A?at=A&ct=C&i=TRACK",
                "https://tv.apple.com/us/movie/ID?playback=resume https://geo.music.apple.com/gb/album/A?i=TRACK",
            ),
            arrayOf(
                "Apple unrelated products do not inherit media policy",
                "https://www.apple.com/shop?at=A&ct=C&utm_source=M",
                "https://www.apple.com/shop?at=A&ct=C",
            ),
            arrayOf(
                "Amazon catalog retains search seller and path referral syntax",
                "https://www.amazon.com/dp/ASIN/ref=path?ascsubtag=A&asc_campaign=A&asc_refurl=A&asc_source=A&linkId=L&creative=C&creativeASIN=C&camp=C&AssociateTag=A&adgrpid=A&hvadid=H&hvbmt=H&hvdev=H&hvlocphy=H&hvnetw=H&hvqmt=H&hvrand=H&hvtargid=H&hydadcr=H&refRID=R&pd_rd_i=I&pd_rd_r=R&pd_rd_w=W&pd_rd_wg=W&pf_rd_i=I&pf_rd_m=M&pf_rd_p=P&pf_rd_r=R&pf_rd_s=S&pf_rd_t=T&pf_rd_w=W&ref=KEEP&ref_=KEEP&sr=1&s=books&keywords=coffee&smid=SELLER",
                "https://www.amazon.com/dp/ASIN/ref=path?ref=KEEP&ref_=KEEP&sr=1&s=books&keywords=coffee&smid=SELLER",
            ),
            arrayOf(
                "Amazon declared regional aliases remove affiliate attribution",
                "https://amazon.com.be/dp/A?tag=T&id=1 https://amazon.ie/dp/B?tag=T&id=2 https://amazon.co.jp/dp/C?tag=T&id=3 https://amazon.com.tr/dp/D?tag=T&id=4",
                "https://amazon.com.be/dp/A?id=1 https://amazon.ie/dp/B?id=2 https://amazon.co.jp/dp/C?id=3 https://amazon.com.tr/dp/D?id=4",
            ),
            arrayOf(
                "invented regional roots are not recognized",
                "https://amazon.invalid/dp/A?tag=T&utm_source=M https://ebay.invalid/itm/1?campid=C&utm_source=M https://google.invalid/search?q=Q&ved=V&utm_source=M",
                "https://amazon.invalid/dp/A?tag=T https://ebay.invalid/itm/1?campid=C https://google.invalid/search?q=Q&ved=V",
            ),
            arrayOf(
                "eBay exact itm path removes attribution but preserves item metadata",
                "https://ebay.co.jp/itm?_trkparms=T&mkevt=M&mkrid=M&customid=C&toolid=T&sssrc=S&ssuid=S&ssspo=S&itmmeta=I&itmprp=I&amdata=A&epid=E&hash=H&var=V",
                "https://ebay.co.jp/itm?epid=E&hash=H&var=V",
            ),
            arrayOf(
                "eBay non-item paths preserve scoped keys",
                "https://ebay.com/sch/items?campid=C&mkcid=M&utm_source=S https://ebay.com/itmore?campid=C&gclid=G",
                "https://ebay.com/sch/items?campid=C&mkcid=M https://ebay.com/itmore?campid=C",
            ),
            arrayOf(
                "Google search catalog retains search filtering",
                "https://google.com/search?q=coffee&ved=V&ei=E&oq=O&esrc=E&uact=U&aqs=A&rlz=R&source=S&sourceid=S&iflsig=I&cshid=C&gs_l=G&gs_lp=G&gs_lcp=G&gs_lcrp=G&sca_esv=S&sclient=S&fbs=F&sstk=S&tbm=isch&tbs=qdr:d&hl=fr&safe=active",
                "https://google.com/search?q=coffee&tbm=isch&tbs=qdr:d&hl=fr&safe=active",
            ),
            arrayOf(
                "Google mobile regional search is eligible",
                "https://m.google.co.uk/search?q=tea&ved=V&source=S https://www.google.cat/search?q=cat&ei=E https://google.com.ua/search?q=Q&gs_lp=G",
                "https://m.google.co.uk/search?q=tea https://www.google.cat/search?q=cat https://google.com.ua/search?q=Q",
            ),
            arrayOf(
                "Google Maps exact host accepts arbitrary paths and retains place IDs",
                "https://maps.google.co.uk/place/P?source=S&g_ep=G&sca_esv=S&api=1&map_action=map&center=1,2&zoom=12&query_place_id=PLACE&destination_place_id=DEST&ved=KEEP",
                "https://maps.google.co.uk/place/P?api=1&map_action=map&center=1,2&zoom=12&query_place_id=PLACE&destination_place_id=DEST&ved=KEEP",
            ),
            arrayOf(
                "Google exact maps path selects only Maps rules",
                "https://google.com/maps?source=S&g_ep=G&sca_esv=S&q=PLACE&ved=KEEP",
                "https://google.com/maps?q=PLACE&ved=KEEP",
            ),
            arrayOf(
                "Google drive and accounts search-like paths reject search rules",
                "https://drive.google.com/search?q=Q&source=S&ved=V&utm_source=M https://accounts.google.com/search?source=S&ei=E&gclid=G",
                "https://drive.google.com/search?q=Q&source=S&ved=V https://accounts.google.com/search?source=S&ei=E",
            ),
            arrayOf(
                "Google restricted host variants reject nested subdomains",
                "https://m.maps.google.com/place/P?source=S&g_ep=G&utm_source=M https://other.www.google.com/search?ved=V&fbclid=F",
                "https://m.maps.google.com/place/P?source=S&g_ep=G https://other.www.google.com/search?ved=V",
            ),
            arrayOf(
                "Google search and maps path boundaries are exact",
                "https://www.google.com/search/extra?ved=V&source=S&utm_source=M https://google.com/mapstore?g_ep=G&source=S&fbclid=F https://google.com/SEARCH?ei=E&gclid=G",
                "https://www.google.com/search/extra?ved=V&source=S https://google.com/mapstore?g_ep=G&source=S https://google.com/SEARCH?ei=E",
            ),
            arrayOf(
                "global Google ad and cross-domain analytics identifiers",
                "https://example.org/?dclid=D&gbraid=G&wbraid=W&gclsrc=G&gad_source=G&gad_campaignid=G&srsltid=S&_gl=G&_ga=G&id=7",
                "https://example.org/?id=7",
            ),
            arrayOf(
                "remaining social and Yandex global click identifiers",
                "https://example.org/?fbadid=F&msclkid=M&ttclid=T&twclid=T&__twitter_impression=T&yclid=Y&ysclid=Y&ym_tracking_id=Y&_openstat=O&id=7",
                "https://example.org/?id=7",
            ),
            arrayOf(
                "Matomo mtm campaign namespace uses only declared suffixes",
                "https://example.org/?mtm_campaign=C&mtm_source=S&mtm_keyword=K&mtm_medium=M&mtm_content=C&mtm_cid=C&mtm_group=G&mtm_placement=P&mtm_custom=KEEP",
                "https://example.org/?mtm_custom=KEEP",
            ),
            arrayOf(
                "Matomo named aliases use the same exact suffix catalog",
                "https://example.org/?matomo_campaign=C&matomo_source=S&matomo_keyword=K&matomo_medium=M&matomo_content=C&matomo_cid=C&matomo_group=G&matomo_placement=P&matomo_custom=KEEP",
                "https://example.org/?matomo_custom=KEEP",
            ),
            arrayOf(
                "Piwik short aliases and visitor ID do not remove arbitrary pk fields",
                "https://example.org/?pk_campaign=C&pk_source=S&pk_keyword=K&pk_medium=M&pk_content=C&pk_cid=C&pk_group=G&pk_placement=P&pk_vid=V&pk_key=KEEP",
                "https://example.org/?pk_key=KEEP",
            ),
            arrayOf(
                "Piwik long aliases do not remove arbitrary piwik fields",
                "https://example.org/?piwik_campaign=C&piwik_source=S&piwik_keyword=K&piwik_medium=M&piwik_content=C&piwik_cid=C&piwik_group=G&piwik_placement=P&piwik_key=KEEP",
                "https://example.org/?piwik_key=KEEP",
            ),
            arrayOf(
                "HubSpot identity CTA and ads catalog retains unrelated hsa keys",
                "https://example.org/?__hsfp=H&__hssc=H&__hstc=H&_hsenc=H&_hsmi=H&hsCtaTracking=H&hsa_acc=H&hsa_ad=H&hsa_cam=H&hsa_grp=H&hsa_kw=H&hsa_la=H&hsa_mt=H&hsa_net=H&hsa_ol=H&hsa_src=H&hsa_tgt=H&hsa_ver=H&hsa_custom=KEEP",
                "https://example.org/?hsa_custom=KEEP",
            ),
            arrayOf(
                "newsletter attribution retains generic customer fields",
                "https://example.org/?mc_cid=M&mc_eid=M&mkt_tok=M&ml_subscriber=M&ml_subscriber_hash=M&vero_id=V&vero_conv=V&oly_anon_id=O&rb_clickid=R&wickedid=W&mc_custom=KEEP&cid=CONTENT",
                "https://example.org/?mc_custom=KEEP&cid=CONTENT",
            ),
            arrayOf(
                "Adobe Mapp Coremetrics exact catalog",
                "https://example.org/?s_cid=S&adobe_mc_ref=A&adobe_mc_sdid=A&wt_mc=W&cm_mmc=C&cm_me=C&cm_cr=C&cid=CONTENT&adobe_custom=KEEP",
                "https://example.org/?cid=CONTENT&adobe_custom=KEEP",
            ),
            arrayOf(
                "affiliate credit catalog retains access-sensitive candidate",
                "https://example.org/?awc=A&cjevent=C&cjdata=C&sscid=S&ir_campaignid=I&ir_adid=I&ir_partnerid=I&ranMID=R&ranEAID=R&ranSiteID=R&irclickid=KEEP",
                "https://example.org/?irclickid=KEEP",
            ),
            arrayOf(
                "AT Internet metadata catalog retains unknown at fields",
                "https://example.org/?xtor=X&at_campaign=A&at_campaign_type=A&at_creation=A&at_emailtype=A&at_link=A&at_link_id=A&at_link_origin=A&at_link_type=A&at_medium=A&at_ptr_name=A&at_recipient_id=A&at_recipient_list=A&at_send_date=A&at_custom=KEEP",
                "https://example.org/?at_custom=KEEP",
            ),
            arrayOf(
                "explicit internal campaign names and mixed-case Echobox",
                "https://example.org/?itm_source=I&itm_medium=I&itm_campaign=I&itm_content=I&itm_term=I&int_source=I&int_medium=I&int_campaign=I&int_content=I&int_term=I&Echobox=E&itm_id=KEEP&int_id=KEEP&echobox=KEEP",
                "https://example.org/?itm_id=KEEP&int_id=KEEP&echobox=KEEP",
            ),
            arrayOf(
                "UTM prefix requires suffix and respects case and literal plus",
                "https://example.org/?utm_custom=X&utm_=KEEP&utm=KEEP&UTM_source=KEEP&utm+source=KEEP&utmx_source=KEEP&ScCid=X&sccid=KEEP&_epik=KEEP",
                "https://example.org/?utm_=KEEP&utm=KEEP&UTM_source=KEEP&utm+source=KEEP&utmx_source=KEEP&sccid=KEEP&_epik=KEEP",
            ),
            arrayOf(
                "literal plus in UTM-prefixed names preserves those entries only",
                "https://example.org/?utm_source+extra=KEEP&utm_source=DROP&utm_+=KEEP",
                "https://example.org/?utm_source+extra=KEEP&utm_+=KEEP",
            ),
            arrayOf(
                "literal non-ASCII UTM-prefixed names survive nearby tracker removal",
                "https://example.org/?utm_café=KEEP&utm_source=DROP&utm_世界=KEEP",
                "https://example.org/?utm_café=KEEP&utm_世界=KEEP",
            ),
            arrayOf(
                "encoded non-ASCII and invalid UTF-8 UTM-prefixed names survive",
                "https://example.org/?utm_%C3%A9=KEEP&utm_source=DROP&utm_%FF=KEEP",
                "https://example.org/?utm_%C3%A9=KEEP&utm_%FF=KEEP",
            ),
            arrayOf(
                "encoded ASCII plus matches UTM prefix while literal plus survives",
                "https://example.org/?utm_source%2Bextra=DROP&utm_source+extra=KEEP",
                "https://example.org/?utm_source+extra=KEEP",
            ),
            arrayOf(
                "reviewed unsafe generic and access-sensitive names survive",
                "https://example.org/?ref=R&source=S&spm=S&cmpid=C&oly_enc_id=O&_branch_match_id=B&_branch_referrer=B&elqTrackId=E&elq=E&utm_source=M",
                "https://example.org/?ref=R&source=S&spm=S&cmpid=C&oly_enc_id=O&_branch_match_id=B&_branch_referrer=B&elqTrackId=E&elq=E",
            ),
            arrayOf(
                "AliExpress exact login hosts preserve ga while removing other globals",
                "https://LOGIN.ALIEXPRESS.COM.:443/?_ga=ACCESS&_gl=G&utm_source=M https://login.aliexpress.us/?%5Fga=ACCESS&gclid=G",
                "https://LOGIN.ALIEXPRESS.COM.:443/?_ga=ACCESS https://login.aliexpress.us/?%5Fga=ACCESS",
            ),
            arrayOf(
                "AliExpress ga exception does not extend to descendants or other hosts",
                "https://m.login.aliexpress.com/?_ga=X&id=1 https://aliexpress.com/?_ga=X&id=2 https://login.aliexpress.com.evil.example/?_ga=X&id=3",
                "https://m.login.aliexpress.com/?id=1 https://aliexpress.com/?id=2 https://login.aliexpress.com.evil.example/?id=3",
            ),
            arrayOf("empty input", "", ""),
            arrayOf("whitespace only", " \t\r\n\u00a0  ", " \t\r\n\u00a0  "),
            arrayOf("plain Unicode text", "Bonjour 世界 🎵 — nothing to clean", "Bonjour 世界 🎵 — nothing to clean"),
            arrayOf(
                "NBSP delimits tokens without changing Unicode messages",
                "🎵\u00a0https://youtu.be/V?si=S\u00a0世界\thttps://example.org/?utm_source=M&id=1\r\nfin",
                "🎵\u00a0https://youtu.be/V\u00a0世界\thttps://example.org/?id=1\r\nfin",
            ),
            arrayOf(
                "unsupported and malformed first tokens do not stop later cleanup",
                "ftp://example.org/?utm_source=X\thttps://example.org/%ZZ?utm_source=X\r\nhttps://youtu.be/V?si=S",
                "ftp://example.org/?utm_source=X\thttps://example.org/%ZZ?utm_source=X\r\nhttps://youtu.be/V",
            ),
            arrayOf(
                "leading middle and trailing runs preserve survivor order",
                "https://example.org/?utm_source=A&gclid=B&x=1&utm_medium=C&fbclid=D&y=2&utm_content=E",
                "https://example.org/?x=1&y=2",
            ),
            arrayOf(
                "all known entries including valueless and empty duplicates disappear",
                "https://example.org/?utm_source&gclid=&utm_source=X&fbclid#fragment",
                "https://example.org/#fragment",
            ),
            arrayOf(
                "duplicate empty and valueless unknown entries survive unchanged",
                "https://example.org/?&flag&x=1&x=2&&blank=&flag&",
                "https://example.org/?&flag&x=1&x=2&&blank=&flag&",
            ),
            arrayOf(
                "leading empty entry survives trailing removed run",
                "https://example.org/?&utm_source=X",
                "https://example.org/?",
            ),
            arrayOf(
                "several empty survivors keep their literal separators",
                "https://example.org/?&&utm_source=X&&",
                "https://example.org/?&&&",
            ),
            arrayOf(
                "empty original query and queryless fragments survive",
                "https://example.org/? https://example.org/?#x https://example.org/#/route?utm_source=X&gclid=G",
                "https://example.org/? https://example.org/?#x https://example.org/#/route?utm_source=X&gclid=G",
            ),
            arrayOf(
                "retained values and nested URLs are opaque",
                "https://example.org/?keep=a%26utm_source%3DX&x=a=b=c&url=https%3A%2F%2Fyoutube.com%2Fwatch%3Fsi%3DS%26utm_source%3DM&utm_source=outer#route?utm_source=fragment&si=S",
                "https://example.org/?keep=a%26utm_source%3DX&x=a=b=c&url=https%3A%2F%2Fyoutube.com%2Fwatch%3Fsi%3DS%26utm_source%3DM#route?utm_source=fragment&si=S",
            ),
            arrayOf(
                "semicolon and encoded separators do not split entries",
                "https://example.org/?id=1;utm_source=X&utm%3Dsource=KEEP&x%26utm_source=KEEP&utm_medium=Y",
                "https://example.org/?id=1;utm_source=X&utm%3Dsource=KEEP&x%26utm_source=KEEP",
            ),
            arrayOf(
                "HTML entities are not decoded but literal ampersands still delimit",
                "https://example.org/?id=1&amp;utm_source=KEEP&utm_source=DROP&amp;gclid=KEEP",
                "https://example.org/?id=1&amp;utm_source=KEEP&amp;gclid=KEEP",
            ),
            arrayOf(
                "value text cannot create tracking names or a signing veto",
                "https://example.org/?note=X-Amz-Signature=S&url=https://youtube.com/watch?si=S&utm_source=M",
                "https://example.org/?note=X-Amz-Signature=S&url=https://youtube.com/watch?si=S",
            ),
            arrayOf(
                "encoded family key removes only canonical case",
                "https://youtube.com/watch?v=V&%73%69=S&SI=KEEP&%2573i=KEEP&%73i+=KEEP",
                "https://youtube.com/watch?v=V&SI=KEEP&%2573i=KEEP&%73i+=KEEP",
            ),
            arrayOf(
                "host lookalikes do not select YouTube rules",
                "https://notyoutube.com/watch?si=S&utm_source=M https://youtu.be.evil.example/V?si=S&is=I&fbclid=F",
                "https://notyoutube.com/watch?si=S https://youtu.be.evil.example/V?si=S&is=I",
            ),
            arrayOf(
                "real host still matches with userinfo and port",
                "http://user:pass@m.youtube.com:8080/watch?v=V&si=S#F",
                "http://user:pass@m.youtube.com:8080/watch?v=V#F",
            ),
            arrayOf(
                "family names in path never select rules",
                "https://example.org/youtube.com/watch?si=S&utm_source=M",
                "https://example.org/youtube.com/watch?si=S",
            ),
            arrayOf(
                "Unicode authority allows global but not product-specific cleanup",
                "https://例え.テスト/watch?si=S&utm_source=M&id=1",
                "https://例え.テスト/watch?si=S&id=1",
            ),
            arrayOf(
                "IPv6 authority still receives global cleanup",
                "http://[2001:db8::1]:8080/a?utm_source=M&si=S&id=1",
                "http://[2001:db8::1]:8080/a?si=S&id=1",
            ),
            arrayOf(
                "missing authorities unsupported schemes and relative links survive",
                "https:/path?utm_source=M https:///path?utm_source=M http:?utm_source=M mailto:u@example.org?utm_source=M ftp://example.org/?utm_source=M /path?utm_source=M //example.org/?utm_source=M",
                "https:/path?utm_source=M https:///path?utm_source=M http:?utm_source=M mailto:u@example.org?utm_source=M ftp://example.org/?utm_source=M /path?utm_source=M //example.org/?utm_source=M",
            ),
            arrayOf(
                "malformed percent escapes veto all edits to that token",
                "https://example.org/?utm_source=M&keep=% https://example.org/?utm_source=M&%7=bad https://example.org/?utm_source=M#bad%GG",
                "https://example.org/?utm_source=M&keep=% https://example.org/?utm_source=M&%7=bad https://example.org/?utm_source=M#bad%GG",
            ),
            arrayOf(
                "arbitrary words markdown email and schemeless links are not scanned",
                "prefixhttps://example.org/?utm_source=M [song](https://youtu.be/V?si=S) u@https://example.org/?utm_source=M youtube.com/watch?si=S",
                "prefixhttps://example.org/?utm_source=M [song](https://youtu.be/V?si=S) u@https://example.org/?utm_source=M youtube.com/watch?si=S",
            ),
            arrayOf(
                "square curly quoted and nested wrappers survive",
                "[https://youtu.be/V?si=S] {https://youtu.be/V?si=S} \"https://youtu.be/V?si=S\" 'https://youtu.be/V?si=S' (<https://youtu.be/V?si=S>).",
                "[https://youtu.be/V] {https://youtu.be/V} \"https://youtu.be/V\" 'https://youtu.be/V' (<https://youtu.be/V>).",
            ),
            arrayOf(
                "each trailing sentence punctuation mark survives",
                "https://youtu.be/V?si=S. https://youtu.be/V?si=S, https://youtu.be/V?si=S! https://youtu.be/V?si=S; https://youtu.be/V?si=S:",
                "https://youtu.be/V. https://youtu.be/V, https://youtu.be/V! https://youtu.be/V; https://youtu.be/V:",
            ),
            arrayOf(
                "unmatched and crossed wrappers leave tokens unchanged",
                "(https://youtu.be/V?si=S [https://youtu.be/V?si=S) ([https://youtu.be/V?si=S)] \"https://youtu.be/V?si=S'",
                "(https://youtu.be/V?si=S [https://youtu.be/V?si=S) ([https://youtu.be/V?si=S)] \"https://youtu.be/V?si=S'",
            ),
            arrayOf(
                "balanced path parentheses are URL content not outer wrappers",
                "https://example.org/a_(b)?utm_source=M&id=1 (https://example.org/a_(b)?utm_source=M).",
                "https://example.org/a_(b)?id=1 (https://example.org/a_(b)).",
            ),
            arrayOf(
                "WhatsApp text Telegram payloads and Vimeo access remain intact",
                "https://wa.me/123?text=hello%20world&utm_source=M https://t.me/bot?start=ACCESS&utm_source=M https://vimeo.com/123?h=ACCESS&t=30&utm_source=M",
                "https://wa.me/123?text=hello%20world https://t.me/bot?start=ACCESS https://vimeo.com/123?h=ACCESS&t=30",
            ),
            arrayOf(
                "opaque short paths are never expanded or interpreted",
                "https://t.co/OPAQUE?utm_source=M&si=S https://spotify.link/OPAQUE?utm_source=M&si=S https://pin.it/OPAQUE?epik=E&access=A https://amzn.to/OPAQUE?utm_source=M&tag=T https://on.soundcloud.com/OPAQUE?utm_source=M&si=S https://vm.tiktok.com/OPAQUE?_t=T&utm_source=M https://vt.tiktok.com/OPAQUE?_r=R&gclid=G",
                "https://t.co/OPAQUE?si=S https://spotify.link/OPAQUE?si=S https://pin.it/OPAQUE?access=A https://amzn.to/OPAQUE?tag=T https://on.soundcloud.com/OPAQUE?si=S https://vm.tiktok.com/OPAQUE?_t=T https://vt.tiktok.com/OPAQUE?_r=R",
            ),
            arrayOf(
                "global-only products retain content access and unproven share names",
                "https://www.pinterest.com/pin/123?epik=E&si=S https://discord.com/invite/ACCESS?utm_source=M&event=1 https://bsky.app/profile/u/post/ID?utm_source=M&ref=R https://mastodon.example/@u/123?utm_source=M&source=S https://soundcloud.com/u/track?utm_source=M&si=S https://deezer.com/track/123?utm_source=M&si=S https://twitch.tv/u?utm_source=M&t=30 https://youtube-nocookie.com/embed/V?si=S&utm_source=M",
                "https://www.pinterest.com/pin/123?si=S https://discord.com/invite/ACCESS?event=1 https://bsky.app/profile/u/post/ID?ref=R https://mastodon.example/@u/123?source=S https://soundcloud.com/u/track?si=S https://deezer.com/track/123?si=S https://twitch.tv/u?t=30 https://youtube-nocookie.com/embed/V?si=S",
            ),
            arrayOf(
                "AWS signature before trackers protects globals and family fields",
                "https://youtube.com/watch?X-Amz-Signature=S&v=V&si=S&is=SHARE&utm_source=M",
                "https://youtube.com/watch?X-Amz-Signature=S&v=V&si=S&is=SHARE&utm_source=M",
            ),
            arrayOf(
                "AWS encoded signature after trackers protects the whole URL",
                "https://storage.example/a?utm_source=M&gclid=G&%58-Amz-Signature=S",
                "https://storage.example/a?utm_source=M&gclid=G&%58-Amz-Signature=S",
            ),
            arrayOf(
                "Google signature before trackers protects the whole URL",
                "https://storage.example/a?X-Goog-Signature=S&utm_source=M&id=1",
                "https://storage.example/a?X-Goog-Signature=S&utm_source=M&id=1",
            ),
            arrayOf(
                "Google signature after trackers protects the whole URL",
                "https://storage.example/a?utm_source=M&id=1&X-Goog-Signature=S",
                "https://storage.example/a?utm_source=M&id=1&X-Goog-Signature=S",
            ),
            arrayOf(
                "encoded Google signature protects family and global removals",
                "https://youtube.com/watch?si=S&is=SHARE&X-Goog-%53ignature=S&utm_source=M&v=V",
                "https://youtube.com/watch?si=S&is=SHARE&X-Goog-%53ignature=S&utm_source=M&v=V",
            ),
            arrayOf(
                "valueless and empty signing keys still protect URLs",
                "https://storage.example/a?utm_source=M&X-Amz-Signature https://storage.example/b?X-Goog-Signature=&gclid=G",
                "https://storage.example/a?utm_source=M&X-Amz-Signature https://storage.example/b?X-Goog-Signature=&gclid=G",
            ),
            arrayOf(
                "signature recognition is case sensitive and decodes only once",
                "https://storage.example/a?x-amz-signature=S&%2558-Goog-Signature=S&utm_source=M&id=1",
                "https://storage.example/a?x-amz-signature=S&%2558-Goog-Signature=S&id=1",
            ),
            arrayOf(
                "signed URL veto is local and does not leak between tokens",
                "https://example.org/?utm_source=A&id=1\thttps://storage.example/a?utm_source=B&X-Amz-Signature=S\r\nhttps://youtu.be/V?si=C https://storage.example/b?X-Goog-Signature=S&utm_source=D https://example.org/?gclid=E&id=2",
                "https://example.org/?id=1\thttps://storage.example/a?utm_source=B&X-Amz-Signature=S\r\nhttps://youtu.be/V https://storage.example/b?X-Goog-Signature=S&utm_source=D https://example.org/?id=2",
            ),
        )
    }
}
