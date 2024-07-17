package com.cyster.adf.writer;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladsch.flexmark.ast.AutoLink;
import com.vladsch.flexmark.ast.BlockQuote;
import com.vladsch.flexmark.ast.BulletList;
import com.vladsch.flexmark.ast.BulletListItem;
import com.vladsch.flexmark.ast.Code;
import com.vladsch.flexmark.ast.CodeBlock;
import com.vladsch.flexmark.ast.Emphasis;
import com.vladsch.flexmark.ast.FencedCodeBlock;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.ast.OrderedList;
import com.vladsch.flexmark.ast.OrderedListItem;
import com.vladsch.flexmark.ast.Paragraph;
import com.vladsch.flexmark.ast.SoftLineBreak;
import com.vladsch.flexmark.ast.StrongEmphasis;
import com.vladsch.flexmark.ast.Text;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.NodeVisitor;
import com.vladsch.flexmark.util.ast.VisitHandler;
import com.vladsch.flexmark.util.ast.Visitor;


// https://www.javadoc.io/doc/com.vladsch.flexmark/flexmark/0.28.22/index.html

class AtlassianDocumentMarkdownVisitor {
    private AtlassianDocumentBuilder builder = new AtlassianDocumentBuilder();
    private NodeVisitor visitor = new NodeVisitor() {
        {
            addHandler(new VisitHandler<Document>(Document.class, new DocumentVisitor()));
            addHandler(new VisitHandler<Paragraph>(Paragraph.class, new ParagraphVisitor()));
            addHandler(new VisitHandler<Heading>(Heading.class, new HeadingVisitor()));
            addHandler(new VisitHandler<Code>(Code.class, new CodeVisitor()));
            addHandler(new VisitHandler<Emphasis>(Emphasis.class, new EmphasisVisitor()));
            addHandler(new VisitHandler<StrongEmphasis>(StrongEmphasis.class, new StrongEmphasisVisitor()));
            addHandler(new VisitHandler<AutoLink>(AutoLink.class, new AutoLinkVisitor()));
            addHandler(new VisitHandler<BlockQuote>(BlockQuote.class, new BlockQuoteVisitor()));
            addHandler(new VisitHandler<FencedCodeBlock>(FencedCodeBlock.class, new FencedCodeBlockVisitor()));
            addHandler(new VisitHandler<CodeBlock>(CodeBlock.class, new CodeBlockVisitor()));
            addHandler(new VisitHandler<BulletList>(BulletList.class, new BulletListVisitor()));
            addHandler(new VisitHandler<BulletListItem>(BulletListItem.class, new BulletListItemVisitor()));
            addHandler(new VisitHandler<OrderedList>(OrderedList.class, new OrderedListVisitor()));
            addHandler(new VisitHandler<OrderedListItem>(OrderedListItem.class, new OrderedListItemVisitor()));

            addHandler(new VisitHandler<Link>(Link.class, new LinkVisitor()));
            addHandler(new VisitHandler<SoftLineBreak>(SoftLineBreak.class, new SoftLineBreakVisitor()));
            addHandler(new VisitHandler<Text>(Text.class, new TextVisitor()));
        }
    };

    JsonNode generate(String markdown) {
        this.builder = new AtlassianDocumentBuilder();

        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown);

        visitor.visit(document);

        return builder.getDocument();
    }

    public String asVisitTree(String markdown) {
        this.builder = new AtlassianDocumentBuilder();

        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown);

        return dumpToString(document, 0);
    }

    private String dumpToString(Node node, int depth) {
        String value = "";
        do {
            value = value + " ".repeat(depth) + "<" + node.getNodeName() + ">\n";

            if (node.getNodeName().equalsIgnoreCase("text")) {
                value = value + " ".repeat(depth + 1) + node.getChars().unescape() + "\n";
            }
            if (node.getNodeName().equalsIgnoreCase("AutoLink")) {
                value = value + " ".repeat(depth + 1) + node.getChars().unescape() + "\n";
            }
            if (node.hasChildren()) {
                value = value + dumpToString(node.getFirstChild(), depth + 1);
            }
            value = value + " ".repeat(depth) + "</" + node.getNodeName() + ">\n";

            node = node.getNext();
        } while(node != null);

        return value;
    }

    public class DocumentVisitor implements Visitor<Document> {
        @Override
        public void visit(Document node) {
            AtlassianDocumentMarkdownVisitor.this.builder.addDocument();
            AtlassianDocumentMarkdownVisitor.this.visitor.visitChildren(node);
        }
    }

    public class ParagraphVisitor implements Visitor<Paragraph> {
        @Override
        public void visit(Paragraph node) {
            AtlassianDocumentMarkdownVisitor.this.builder.startParagraph();
            AtlassianDocumentMarkdownVisitor.this.visitor.visitChildren(node);
            AtlassianDocumentMarkdownVisitor.this.builder.endParagraph();
        }
    }

    public class HeadingVisitor implements Visitor<Heading> {
        @Override
        public void visit(Heading node) {
            AtlassianDocumentMarkdownVisitor.this.builder.startHeading(node.getLevel());
            AtlassianDocumentMarkdownVisitor.this.visitor.visitChildren(node);
            AtlassianDocumentMarkdownVisitor.this.builder.endHeading();
        }
    }

    public class CodeVisitor implements Visitor<Code> {
        @Override
        public void visit(Code node) {
            AtlassianDocumentMarkdownVisitor.this.builder.startCode();
            AtlassianDocumentMarkdownVisitor.this.visitor.visitChildren(node);
            AtlassianDocumentMarkdownVisitor.this.builder.endCode();
        }
    }

    public class EmphasisVisitor implements Visitor<Emphasis> {
        @Override
        public void visit(Emphasis node) {
            AtlassianDocumentMarkdownVisitor.this.builder.startEmphasis();
            AtlassianDocumentMarkdownVisitor.this.visitor.visitChildren(node);
            AtlassianDocumentMarkdownVisitor.this.builder.endEmphasis();
        }
    }

    public class StrongEmphasisVisitor implements Visitor<StrongEmphasis> {
        @Override
        public void visit(StrongEmphasis node) {
            AtlassianDocumentMarkdownVisitor.this.builder.startStrong();
            AtlassianDocumentMarkdownVisitor.this.visitor.visitChildren(node);
            AtlassianDocumentMarkdownVisitor.this.builder.endStrong();
        }
    }

    public class AutoLinkVisitor implements Visitor<AutoLink> {
        @Override
        public void visit(AutoLink node) {
            AtlassianDocumentMarkdownVisitor.this.builder.startLink(node.getUrl().unescape());
            AtlassianDocumentMarkdownVisitor.this.builder.addText(node.getUrl().unescape());
            AtlassianDocumentMarkdownVisitor.this.visitor.visitChildren(node);
            AtlassianDocumentMarkdownVisitor.this.builder.endLink();
        }
    }

    public class LinkVisitor implements Visitor<Link> {
        @Override
        public void visit(Link node) {
            AtlassianDocumentMarkdownVisitor.this.builder.startLink(node.getUrl().unescape());
            AtlassianDocumentMarkdownVisitor.this.visitor.visitChildren(node);
            AtlassianDocumentMarkdownVisitor.this.builder.endLink();
        }
    }

    public class BlockQuoteVisitor implements Visitor<BlockQuote> {
        @Override
        public void visit(BlockQuote node) {
            AtlassianDocumentMarkdownVisitor.this.builder.startBlockQuote();
            AtlassianDocumentMarkdownVisitor.this.visitor.visitChildren(node);
            AtlassianDocumentMarkdownVisitor.this.builder.endBlockQuote();
        }
    }

    public class FencedCodeBlockVisitor implements Visitor<FencedCodeBlock> {
        @Override
        public void visit(FencedCodeBlock node) {
            String language = node.getInfo().unescape();
            if (language == null || language.isEmpty()) {
                AtlassianDocumentMarkdownVisitor.this.builder.startCodeBlock();
            } else {
                AtlassianDocumentMarkdownVisitor.this.builder.startCodeBlock(node.getInfo().unescape());
            }
            AtlassianDocumentMarkdownVisitor.this.builder.addText(
                node.getContentChars().toString().replaceAll("\\n+$", ""));
            AtlassianDocumentMarkdownVisitor.this.builder.endCodeBlock();
        }
    }

    public class CodeBlockVisitor implements Visitor<CodeBlock> {
        @Override
        public void visit(CodeBlock node) {
            AtlassianDocumentMarkdownVisitor.this.builder.startCodeBlock();
            AtlassianDocumentMarkdownVisitor.this.visitor.visitChildren(node);
            AtlassianDocumentMarkdownVisitor.this.builder.addText(
                node.getContentChars().toString().replaceAll("\\n+$", ""));
            AtlassianDocumentMarkdownVisitor.this.builder.endCodeBlock();
        }
    }

    public class BulletListVisitor implements Visitor<BulletList> {
        @Override
        public void visit(BulletList node) {
            AtlassianDocumentMarkdownVisitor.this.builder.startBulletList();
            AtlassianDocumentMarkdownVisitor.this.visitor.visitChildren(node);
            AtlassianDocumentMarkdownVisitor.this.builder.endBulletList();

        }
    }

    public class BulletListItemVisitor implements Visitor<BulletListItem> {
        @Override
        public void visit(BulletListItem node) {
            AtlassianDocumentMarkdownVisitor.this.builder.startListItem();
            AtlassianDocumentMarkdownVisitor.this.visitor.visitChildren(node);
            AtlassianDocumentMarkdownVisitor.this.builder.endListItem();
        }
    }

    public class OrderedListVisitor implements Visitor<OrderedList> {
        @Override
        public void visit(OrderedList node) {
            AtlassianDocumentMarkdownVisitor.this.builder.startOrderedList();
            AtlassianDocumentMarkdownVisitor.this.visitor.visitChildren(node);
            AtlassianDocumentMarkdownVisitor.this.builder.endOrderedList();
        }
    }

    public class OrderedListItemVisitor implements Visitor<OrderedListItem> {
        @Override
        public void visit(OrderedListItem node) {
            AtlassianDocumentMarkdownVisitor.this.builder.startListItem();
            AtlassianDocumentMarkdownVisitor.this.visitor.visitChildren(node);
            AtlassianDocumentMarkdownVisitor.this.builder.endListItem();
        }
    }

    public class SoftLineBreakVisitor implements Visitor<SoftLineBreak> {
         @Override
         public void visit(SoftLineBreak node) {
             AtlassianDocumentMarkdownVisitor.this.builder.addBreak();
         }
    }

    public class TextVisitor implements Visitor<Text> {
        @Override
        public void visit(Text node) {
            AtlassianDocumentMarkdownVisitor.this.builder.addText(node.getChars().unescape());
            AtlassianDocumentMarkdownVisitor.this.visitor.visitChildren(node);
        }
    }


}
