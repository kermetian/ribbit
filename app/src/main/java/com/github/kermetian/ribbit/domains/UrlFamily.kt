package com.github.kermetian.ribbit.domains

/** Independently curated, case-sensitive query names; provenance lives in docs/url-tracking-research.md. */
internal sealed class UrlFamily {
    abstract fun removes(parameter: String): Boolean

    sealed class Exact(vararg names: String) : UrlFamily() {
        private val parameters = names.toSet()
        override fun removes(parameter: String): Boolean = parameter in parameters
    }

    object YouTube : Exact("si", "feature", "pp", "is")
    object YouTubeShort : Exact("si", "feature", "pp", "is")
    object Spotify : Exact("si")
    object SpotifyOpen : Exact("si", "sp_cid", "dlsi", "pi", "referral")
    object Facebook : Exact("mibextid", "sfnsn", "__tn__", "comment_tracking") {
        private val indexedAttribution = Regex("^__(?:cft|xts)__\\[[0-9]+\\]$")
        override fun removes(parameter: String): Boolean =
            super.removes(parameter) || indexedAttribution.matches(parameter)
    }
    object Instagram : Exact("igsh", "igshid")
    object ThreadsLegacy : Exact("igshid")
    object Twitter : Exact("s", "t", "src", "ref_src", "refsrc", "ref_url", "cn")
    object X : Exact("s", "t", "src", "ref_src", "refsrc", "ref_url", "cn", "a")
    object Reddit : Exact(
        "share_id", "ref_source", "ref_campaign", "correlation_id", "rdt", "_branch_match_id", "entry_point"
    )
    object TikTok : Exact(
        "_t", "_r", "_d", "u_code", "share_iid", "share_link_id", "share_app_name", "share_region",
        "social_share_type", "tt_from", "trackParams", "ug_btm", "is_from_webapp", "sender_device"
    )
    object LinkedIn : Exact("trk", "trackingId", "refId")
    object Snapchat : Exact("sc_referrer", "sc_ua")
    object Bilibili : Exact(
        "spm_id_from", "vd_source", "share_source", "share_medium", "share_plat", "share_tag",
        "share_session_id", "buvid", "unique_k"
    )
    object BilibiliSpace : Exact("spm_id_from")
    object Zhihu : Exact("share_code")
    object Medium : Exact("source")
    object IMDb : Exact("ref_", "pf_rd_i", "pf_rd_m", "pf_rd_p", "pf_rd_r", "pf_rd_s", "pf_rd_t")
    object Netflix : Exact("trackId", "tctx")
    object MSN : Exact("ocid", "cvid")
    object Cnet : Exact("ftag")
    object SteamStore : Exact("snr")
    object AppleMedia : Exact("at", "ct")
    object Amazon : Exact(
        "tag", "ascsubtag", "asc_campaign", "asc_refurl", "asc_source", "linkCode", "linkId",
        "creative", "creativeASIN", "camp", "AssociateTag", "adgrpid", "hvadid", "hvbmt", "hvdev",
        "hvlocphy", "hvnetw", "hvqmt", "hvrand", "hvtargid", "hydadcr", "refRID", "pd_rd_i",
        "pd_rd_r", "pd_rd_w", "pd_rd_wg", "pf_rd_i", "pf_rd_m", "pf_rd_p", "pf_rd_r", "pf_rd_s",
        "pf_rd_t", "pf_rd_w"
    )
    object Ebay : Exact(
        "_trkparms", "mkevt", "mkcid", "mkrid", "campid", "customid", "toolid", "sssrc", "ssuid",
        "ssspo", "itmmeta", "itmprp", "amdata"
    )
    object GoogleSearch : Exact(
        "ved", "ei", "oq", "esrc", "uact", "aqs", "rlz", "source", "sourceid", "iflsig", "cshid",
        "gs_l", "gs_lp", "gs_lcp", "gs_lcrp", "sca_esv", "sclient", "fbs", "sstk"
    )
    object GoogleMaps : Exact("source", "g_ep", "sca_esv")
    object GoogleMapsShort : Exact("g_st")
    object BingSearch : Exact("cvid", "sk", "sp", "sc", "qs", "qp", "form")
}
