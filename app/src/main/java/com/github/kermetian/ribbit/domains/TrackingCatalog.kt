package com.github.kermetian.ribbit.domains

internal object TrackingCatalog {
    private val global = buildSet {
        addAll(
            listOf(
                "gclid", "dclid", "gbraid", "wbraid", "gclsrc", "gad_source", "gad_campaignid", "srsltid", "_gl", "_ga",
                "fbclid", "fbadid", "msclkid", "ttclid", "twclid", "li_fat_id", "epik", "ScCid", "__twitter_impression",
                "yclid", "ysclid", "ym_tracking_id", "_openstat", "pk_vid",
                "__hsfp", "__hssc", "__hstc", "_hsenc", "_hsmi", "hsCtaTracking", "hsa_acc", "hsa_ad", "hsa_cam",
                "hsa_grp", "hsa_kw", "hsa_la", "hsa_mt", "hsa_net", "hsa_ol", "hsa_src", "hsa_tgt", "hsa_ver",
                "mc_cid", "mc_eid", "mkt_tok", "ml_subscriber", "ml_subscriber_hash", "vero_id", "vero_conv",
                "oly_anon_id", "rb_clickid", "wickedid", "s_cid", "adobe_mc_ref", "adobe_mc_sdid", "wt_mc",
                "cm_mmc", "cm_me", "cm_cr", "awc", "cjevent", "cjdata", "sscid", "ir_campaignid", "ir_adid",
                "ir_partnerid", "ranMID", "ranEAID", "ranSiteID", "xtor", "at_campaign", "at_campaign_type",
                "at_creation", "at_emailtype", "at_link", "at_link_id", "at_link_origin", "at_link_type", "at_medium",
                "at_ptr_name", "at_recipient_id", "at_recipient_list", "at_send_date",
                "itm_source", "itm_medium", "itm_campaign", "itm_content", "itm_term",
                "int_source", "int_medium", "int_campaign", "int_content", "int_term", "Echobox"
            )
        )
        for (prefix in listOf("mtm_", "matomo_", "pk_", "piwik_")) {
            for (suffix in listOf("campaign", "source", "keyword", "medium", "content", "cid", "group", "placement")) {
                add(prefix + suffix)
            }
        }
    }

    private val redditPost = Regex("^/(?:r/[^/]+/|user/[^/]+/)?comments/[^/]+(?:/.*)?$")
    private val tikTokVideo = Regex("^/@[^/]+/video/[0-9]+(?:/.*)?$")

    private val families = buildMap {
        put("youtube.com", UrlFamily.YouTube)
        put("youtu.be", UrlFamily.YouTubeShort)
        put("spotify.com", UrlFamily.Spotify)
        put("open.spotify.com", UrlFamily.SpotifyOpen)
        put("facebook.com", UrlFamily.Facebook)
        put("instagram.com", UrlFamily.Instagram)
        put("threads.net", UrlFamily.ThreadsLegacy)
        put("twitter.com", UrlFamily.Twitter)
        put("x.com", UrlFamily.X)
        put("reddit.com", UrlFamily.Reddit)
        put("tiktok.com", UrlFamily.TikTok)
        put("linkedin.com", UrlFamily.LinkedIn)
        put("snapchat.com", UrlFamily.Snapchat)
        put("bilibili.com", UrlFamily.Bilibili)
        put("zhihu.com", UrlFamily.Zhihu)
        put("medium.com", UrlFamily.Medium)
        put("imdb.com", UrlFamily.IMDb)
        put("netflix.com", UrlFamily.Netflix)
        put("msn.com", UrlFamily.MSN)
        put("cnet.com", UrlFamily.Cnet)
        put("steampowered.com", UrlFamily.SteamStore)
        put("music.apple.com", UrlFamily.AppleMedia)
        put("tv.apple.com", UrlFamily.AppleMedia)
        put("maps.app.goo.gl", UrlFamily.GoogleMapsShort)
        put("bing.com", UrlFamily.BingSearch)
        for (root in """
            amazon.com amazon.co.uk amazon.com.au amazon.com.be amazon.eg amazon.ie
            amazon.in amazon.pl amazon.sa amazon.se amazon.sg amazon.co.jp amazon.com.mx
            amazon.es amazon.com.br amazon.ca amazon.com.tr amazon.fr amazon.de amazon.nl
            amazon.it amazon.ae
        """.trimIndent().split(Regex("\\s+"))) {
            put(root, UrlFamily.Amazon)
        }
        for (root in """
            ebay.com ebay.ca ebay.co.uk ebay.com.au ebay.at ebay.be ebay.fr ebay.de
            ebay.it ebay.nl ebay.es ebay.ch ebay.com.hk ebay.ie ebay.ph ebay.pl
            ebay.co.th ebay.vn ebay.co.jp
        """.trimIndent().split(Regex("\\s+"))) {
            put(root, UrlFamily.Ebay)
        }
        for (root in """
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
        """.trimIndent().split(Regex("\\s+"))) {
            put(root, UrlFamily.GoogleSearch)
        }
    }

    fun isGlobal(parameter: String, host: String?): Boolean {
        if (parameter == "_ga" && (host == "login.aliexpress.com" || host == "login.aliexpress.us")) {
            return false
        }
        return (parameter.length > 4 && parameter.startsWith("utm_")) || parameter in global
    }

    /** Most-specific registered root wins, including when its eligibility rejects this URL. */
    fun familyFor(host: String, rawPath: String): UrlFamily? {
        var root = host
        while (true) {
            val family = families[root]
            if (family != null) return eligible(family, host, root, rawPath)
            val dot = root.indexOf('.')
            if (dot < 0) return null
            root = root.substring(dot + 1)
        }
    }

    private fun eligible(family: UrlFamily, host: String, root: String, path: String): UrlFamily? =
        when (family) {
            UrlFamily.YouTube -> family.unless(path.isAtOrBelow("/signin"))
            UrlFamily.SpotifyOpen -> if (host == root) family else UrlFamily.Spotify
            UrlFamily.Twitter, UrlFamily.X -> family.unless(path.isAtOrBelow("/i/redirect"))
            UrlFamily.Reddit -> family.unless(!redditPost.matches(path))
            UrlFamily.TikTok -> family.unless(!tikTokVideo.matches(path))
            UrlFamily.Bilibili -> when {
                host.isAtOrBelowHost("api.bilibili.com") -> null
                host.isAtOrBelowHost("space.bilibili.com") -> UrlFamily.BilibiliSpace
                else -> family
            }
            UrlFamily.Ebay -> family.unless(!path.isAtOrBelow("/itm"))
            UrlFamily.GoogleSearch -> when {
                host == "maps.$root" -> UrlFamily.GoogleMaps
                host != root && host != "www.$root" && host != "m.$root" -> null
                path == "/search" -> family
                path.isAtOrBelow("/maps") -> UrlFamily.GoogleMaps
                else -> null
            }
            UrlFamily.GoogleMapsShort -> family.unless(host != root)
            UrlFamily.BingSearch -> family.unless(path != "/search")
            else -> family
        }

    private fun UrlFamily.unless(excluded: Boolean): UrlFamily? = if (excluded) null else this
    private fun String.isAtOrBelow(path: String): Boolean = this == path || startsWith("$path/")
    private fun String.isAtOrBelowHost(root: String): Boolean = this == root || endsWith(".$root")
}
