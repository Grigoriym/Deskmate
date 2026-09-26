package com.grappim.deskmate.core.api.dto

/**
 * Verbatim copy of `../esp32-desk-display/docs/api/status.example.json`, as of esp32-desk-display
 * commit `cf9740c992878e3d6f9e06e81f4be91dffb0d162` (the last commit that changed it, 2026-09-26).
 * The firmware's host test keeps that file equal to its real output. When it changes there,
 * copy it here again and update the commit.
 */
internal val statusExampleJson: String =
    """
    {
      "time": "17:42",
      "date": "2026-09-26",
      "screen": "home",
      "panel_on": true,
      "outdoor": {
        "temp_c": 14,
        "weather_code": 61,
        "wind_kmh": 18,
        "uv_max": 3,
        "sunrise": "06:58",
        "sunset": "18:55",
        "rain": {
          "in_h": 0,
          "from": "17:00",
          "until": "20:00"
        }
      },
      "indoor": {
        "temp_c": 23.1,
        "humidity_pct": 44.9,
        "pressure_hpa": 1014.2
      },
      "air": {
        "aqi": 31,
        "aqi_label": "FAIR",
        "pollen": {
          "alder": 0,
          "birch": 0,
          "grass": 12,
          "mugwort": 3,
          "ragweed": 0
        }
      },
      "warning": {
        "count": 1,
        "event": "HEAVY RAIN",
        "severity": "moderate",
        "started": false,
        "onset": "19:00"
      },
      "next_holiday": {
        "date": "2026-10-03",
        "name": "GERMAN UNITY DAY"
      },
      "bvg": {
        "walk_min": 6,
        "walk_comfort": 11,
        "departures": [
          {
            "line": "U5",
            "direction": "HAUPTBAHNHOF",
            "time": "17:45",
            "in_min": 3
          },
          {
            "line": "U5",
            "direction": "HAUPTBAHNHOF",
            "time": "17:55",
            "in_min": 13
          }
        ]
      }
    }
    """.trimIndent()
