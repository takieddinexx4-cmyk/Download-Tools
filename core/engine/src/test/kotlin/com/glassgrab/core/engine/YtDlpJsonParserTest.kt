package com.glassgrab.core.engine

import com.glassgrab.core.domain.model.Platform
import com.glassgrab.core.domain.model.VideoCodec
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class YtDlpJsonParserTest {

    private val parser = YtDlpJsonParser()

    @Test
    fun `parses TikTok JSON preferring highest resolution and bitrate no-watermark stream`() {
        val json = """
            {
              "id": "7382910293",
              "title": "HQ QUALITE TEST",
              "uploader": "@taki_eddi_ne",
              "duration": 15.0,
              "thumbnail": "https://example.com/thumb.jpg",
              "formats": [
                {
                  "format_id": "h264_540p_low",
                  "ext": "mp4",
                  "width": 576,
                  "height": 1024,
                  "fps": 30,
                  "tbr": 850,
                  "vcodec": "h264",
                  "acodec": "aac",
                  "filesize": 1500000
                },
                {
                  "format_id": "bytevc1_1080p_hq",
                  "ext": "mp4",
                  "width": 1080,
                  "height": 1920,
                  "fps": 60,
                  "tbr": 7800,
                  "vcodec": "h265",
                  "acodec": "aac",
                  "filesize": 14500000
                },
                {
                  "format_id": "play_watermark-1",
                  "ext": "mp4",
                  "width": 1080,
                  "height": 1920,
                  "fps": 30,
                  "vcodec": "avc1",
                  "acodec": "mp4a.40.2",
                  "filesize": 12000000,
                  "format_note": "watermarked"
                }
              ]
            }
        """.trimIndent()

        val info = parser.parseDumpJson(json, "https://www.tiktok.com/@taki_eddi_ne/video/7382910293")

        assertEquals(Platform.TIKTOK, info.platform)
        assertTrue(info.formats.first().isBestOriginal)
        assertTrue(info.formats.first().isNoWatermark)
        assertEquals(1080, info.formats.first().resolutionHeight)
        assertEquals("1080×1920", info.formats.first().dimensionsLabel)
        assertEquals(7800, info.formats.first().bitrateKbps)
        assertEquals(VideoCodec.HEVC, info.formats.first().videoCodec)
        assertFalse(info.formats.any { it.formatId == "play_watermark-1" })
    }

    @Test
    fun `parses YouTube separate video and audio streams with merge flag`() {
        val json = """
            {
              "id": "yt123",
              "title": "8K Nature",
              "channel": "DemoChannel",
              "duration": 180,
              "subtitles": { "en": [], "ar": [] },
              "formats": [
                {
                  "format_id": "571",
                  "ext": "mp4",
                  "width": 7680,
                  "height": 4320,
                  "fps": 60,
                  "vbr": 45000,
                  "vcodec": "av01.0.16M.08",
                  "acodec": "none",
                  "filesize": 500000000
                },
                {
                  "format_id": "251",
                  "ext": "webm",
                  "vcodec": "none",
                  "acodec": "opus",
                  "abr": 160,
                  "filesize": 3500000
                }
              ]
            }
        """.trimIndent()

        val info = parser.parseDumpJson(json, "https://www.youtube.com/watch?v=yt123")

        assertEquals(Platform.YOUTUBE, info.platform)
        assertTrue(info.hasSubtitles)
        assertEquals(listOf("en", "ar"), info.subtitleLanguages)
        val eightK = info.formats.first { it.formatId == "571" }
        assertTrue(eightK.requiresMerge)
        assertEquals("571+bestaudio/best", eightK.ytDlpSelector)
        assertEquals(VideoCodec.AV1, eightK.videoCodec)
        assertEquals(4320, eightK.resolutionHeight)
    }

    @Test
    fun `parses playlist JSON into batch entries`() {
        val json = """
            {
              "_type": "playlist",
              "id": "PL123",
              "title": "Design Talks",
              "uploader": "GlassChannel",
              "entries": [
                { "id": "v1", "url": "https://youtu.be/v1", "title": "Part 1", "duration": 60 },
                { "id": "v2", "url": "https://youtu.be/v2", "title": "Part 2", "duration": 90 }
              ]
            }
        """.trimIndent()

        val info = parser.parseDumpJson(json, "https://www.youtube.com/playlist?list=PL123")

        assertTrue(info.isPlaylist)
        assertEquals(2, info.playlistEntries.size)
        assertEquals(150L, info.durationSeconds)
    }
}
