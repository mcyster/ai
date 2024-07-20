package com.cyster.adf.writer;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.cyster.jira.client.adf.writer.AtlassianDocumentMapper;
import com.cyster.jira.client.adf.writer.AtlassianDocumentMarkdownVisitor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

// https://commonmark.org/help/

public class AtlassianDocumentMapperTest {

    @Test
    public void testText() {
        var expectedResult = """
{
  "version" : 1,
  "type" : "doc",
  "content" : [ {
    "type" : "paragraph",
    "content" : [ {
      "type" : "text",
      "text" : "This is Sparta"
    } ]
  } ]
}
""";
        var markdown = "This is Sparta ";

        checkResult(markdown, expectedResult);
    }


    @Test
    public void testHeading1() {
        var expectedResult = """
                {
  "version" : 1,
  "type" : "doc",
  "content" : [ {
    "type" : "heading",
    "content" : [ {
      "type" : "text",
      "text" : "Title"
    } ],
    "attrs" : {
      "level" : 1
    }
  }, {
    "type" : "paragraph",
    "content" : [ {
      "type" : "text",
      "text" : "test"
    } ]
  } ]
}
""";
        var markdown = "# Title\ntest";

        checkResult(markdown, expectedResult);
    }

    @Test
    public void testHeading3() {
        var expectedResult = """
                {
  "version" : 1,
  "type" : "doc",
  "content" : [ {
    "type" : "heading",
    "content" : [ {
      "type" : "text",
      "text" : "Title"
    } ],
    "attrs" : {
      "level" : 3
    }
  }, {
    "type" : "paragraph",
    "content" : [ {
      "type" : "text",
      "text" : "test"
    } ]
  } ]
}
""";
        var markdown = "### Title\ntest";

        checkResult(markdown, expectedResult);
    }

    @Test
    public void testWithEmphasisAndStrongEmphasis() {
        var expectedResult = """
{
  "version" : 1,
  "type" : "doc",
  "content" : [ {
    "type" : "paragraph",
    "content" : [ {
      "type" : "text",
      "text" : "text "
    }, {
      "type" : "text",
      "text" : "emphasis",
      "marks" : [ {
        "type" : "em"
      } ]
    }, {
      "type" : "text",
      "text" : " after "
    }, {
      "type" : "text",
      "text" : "bold",
      "marks" : [ {
        "type" : "strong"
      } ]
    }, {
      "type" : "text",
      "text" : " and "
    }, {
      "type" : "text",
      "text" : "crazy ",
      "marks" : [ {
        "type" : "em"
      } ]
    }, {
      "type" : "text",
      "text" : "both",
      "marks" : [ {
        "type" : "em"
      }, {
        "type" : "strong"
      } ]
    }, {
      "type" : "text",
      "text" : " emphasis",
      "marks" : [ {
        "type" : "em"
      } ]
    } ]
  } ]
}
""";
        var markdown = "text *emphasis* after **bold** and *crazy **both** emphasis*";

        checkResult(markdown, expectedResult);
    }

    @Test
    public void testCode() {
        var expectedResult = """
{
  "version" : 1,
  "type" : "doc",
  "content" : [ {
    "type" : "paragraph",
    "content" : [ {
      "type" : "text",
      "text" : "Check this out: "
    }, {
      "type" : "text",
      "text" : "alert(\\"testing\\");",
      "marks" : [ {
        "type" : "code"
      } ]
    } ]
  } ]
}
""";

        var markdown = "Check this out: `alert(\"testing\");`";

        checkResult(markdown, expectedResult);
    }

    @Test
    public void testCodeBlock() {
        var expectedResult = """
{
  "version" : 1,
  "type" : "doc",
  "content" : [ {
    "type" : "paragraph",
    "content" : [ {
      "type" : "text",
      "text" : "Check this out:"
    } ]
  }, {
    "type" : "codeBlock",
    "content" : [ {
      "type" : "text",
      "text" : "alert(\\"testing\\");"
    } ]
  } ]
}
""";

        var markdown = """
Check this out:
```
alert("testing");
```
""";


        checkResult(markdown, expectedResult);
    }

    @Test
    public void testFencedCodeBlock() {
        var expectedResult = """
{
  "version" : 1,
  "type" : "doc",
  "content" : [ {
    "type" : "paragraph",
    "content" : [ {
      "type" : "text",
      "text" : "Check this out:"
    } ]
  }, {
    "type" : "codeBlock",
    "content" : [ {
      "type" : "text",
      "text" : "alert(\\"testing\\");"
    } ],
    "attrs" : {
      "language" : "javascript"
    }
  } ]
}
""";

        var markdown = """
Check this out:
```javascript
alert("testing");
```
""";


        checkResult(markdown, expectedResult);
    }

    @Test
    public void testLink() {
        var expectedResult = """
{
  "version" : 1,
  "type" : "doc",
  "content" : [ {
    "type" : "paragraph",
    "content" : [ {
      "type" : "text",
      "text" : "Check this link: "
    }, {
      "type" : "text",
      "text" : "https://github.com/mcyster/ai",
      "marks" : [ {
        "type" : "link",
        "attrs" : {
          "href" : "https://github.com/mcyster/ai"
        }
      } ]
    } ]
  } ]
}
""";
        var markdown = """
Check this link: <https://github.com/mcyster/ai>
""";

        checkResult(markdown, expectedResult);
    }

    @Test
    public void testFormattedLink() {
        var expectedResult = """
{
  "version" : 1,
  "type" : "doc",
  "content" : [ {
    "type" : "paragraph",
    "content" : [ {
      "type" : "text",
      "text" : "Check this "
    }, {
      "type" : "text",
      "text" : "AI Cyster",
      "marks" : [ {
        "type" : "link",
        "attrs" : {
          "href" : "https://github.com/mcyster/ai"
        }
      } ]
    } ]
  } ]
}
""";
        var markdown = """
Check this [AI Cyster](https://github.com/mcyster/ai)
""";

        checkResult(markdown, expectedResult);
    }

    @Test
    public void testBlockQuote() {
        // nested block quotes - perhaps ADF doesn't support?
        var expectedResult = """
{
  "version" : 1,
  "type" : "doc",
  "content" : [ {
    "type" : "paragraph",
    "content" : [ {
      "type" : "text",
      "text" : "Some block quotes"
    } ]
  }, {
    "type" : "blockquote",
    "content" : [ {
      "type" : "paragraph",
      "content" : [ {
        "type" : "text",
        "text" : "First line"
      }, {
        "type" : "hardBreak"
      }, {
        "type" : "text",
        "text" : "Second Line"
      } ]
    }, {
      "type" : "paragraph",
      "content" : [ {
        "type" : "text",
        "text" : "List Line"
      } ]
    } ]
  } ]
}
""";
        var markdown = """
Some block quotes
> First line
> Second Line
>
> List Line
""";

        checkResult(markdown, expectedResult);
    }

    @Test
    public void testBulletList() {
        var expectedResult = """
{
  "version" : 1,
  "type" : "doc",
  "content" : [ {
    "type" : "paragraph",
    "content" : [ {
      "type" : "text",
      "text" : "List:"
    } ]
  }, {
    "type" : "bulletList",
    "content" : [ {
      "type" : "listItem",
      "content" : [ {
        "type" : "paragraph",
        "content" : [ {
          "type" : "text",
          "text" : "item1"
        } ]
      } ]
    }, {
      "type" : "listItem",
      "content" : [ {
        "type" : "paragraph",
        "content" : [ {
          "type" : "text",
          "text" : "item2"
        } ]
      } ]
    }, {
      "type" : "listItem",
      "content" : [ {
        "type" : "paragraph",
        "content" : [ {
          "type" : "text",
          "text" : "item3"
        } ]
      } ]
    } ]
  } ]
}
""";
        var markdown = """
List:
* item1
* item2
* item3
""";

        checkResult(markdown, expectedResult);
    }

    @Test
    public void testOrderedList() {
        var expectedResult = """
{
  "version" : 1,
  "type" : "doc",
  "content" : [ {
    "type" : "paragraph",
    "content" : [ {
      "type" : "text",
      "text" : "List:"
    } ]
  }, {
    "type" : "orderedList",
    "content" : [ {
      "type" : "listItem",
      "content" : [ {
        "type" : "paragraph",
        "content" : [ {
          "type" : "text",
          "text" : "item1"
        } ]
      } ]
    }, {
      "type" : "listItem",
      "content" : [ {
        "type" : "paragraph",
        "content" : [ {
          "type" : "text",
          "text" : "item2"
        } ]
      } ]
    }, {
      "type" : "listItem",
      "content" : [ {
        "type" : "paragraph",
        "content" : [ {
          "type" : "text",
          "text" : "item3"
        } ]
      } ]
    } ]
  } ]
}

""";
        var markdown = """
List:
1. item1
1. item2
1. item3
""";

        checkResult(markdown, expectedResult);
    }

    private void checkResult(String markdown, String expectedResult) {
        dump(markdown);

        ObjectMapper jsonMapper = new ObjectMapper();
        AtlassianDocumentMapper mapper = new AtlassianDocumentMapper();

        var result = mapper.fromMarkdown(markdown);

        System.out.println(result.toPrettyString());

        try {
            assertTrue(result.equals(jsonMapper.readTree(expectedResult)), "Generated Json did not match expected result");
        } catch (JsonProcessingException exception) {
            fail("Failed to parse expectedResult", exception);
        }
    }

    private void dump(String markdown) {
        var visitor = new AtlassianDocumentMarkdownVisitor();

        System.out.println("\n");
        System.out.println(markdown + "\n\n");
        System.out.println(visitor.asVisitTree(markdown) + "\n");
    }
}
