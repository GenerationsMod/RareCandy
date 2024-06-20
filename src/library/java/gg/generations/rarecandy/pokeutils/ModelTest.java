package gg.generations.rarecandy.pokeutils;

public class ModelTest {
    public static void main(String[] args) {
        var json = """
                {
                  "scale": 1.0,
                  "materials": {
                    "body": {
                      "shader": "masked",
                      "cull": "None",
                      "blend": "None",
                      "images": {
                        "diffuse": "body_alb.png",
                        "mask": "body_msk.png"
                      },
                      "values": {
                        "color": [
                          1.0,
                          1.0,
                          1.0
                        ]
                      }
                    },
                    "screen": {
                      "shader": "solid",
                      "cull": "None",
                      "blend": "None",
                      "images": {
                        "diffuse": "screen.png"
                      },
                      "values": {}
                    }
                  },
                  "defaultVariant": {
                    "screen": {
                      "material": "screen",
                      "hide": false
                    },
                    "body": {
                      "material": "body",
                      "hide": false
                    }
                  },
                  "variants": {
                    "on": {},
                    "off": {
                      "screen": {
                        "offset": [
                          0.0,
                          0.5
                        ]
                      }
                    }
                  }
                }
                """;
        var obj = (PixelAsset.GSON.fromJson(json, ModelConfig.class));
        System.out.println();


    }
}
