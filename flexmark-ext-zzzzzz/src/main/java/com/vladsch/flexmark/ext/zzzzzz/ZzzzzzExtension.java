package com.vladsch.flexmark.ext.zzzzzz;

import com.vladsch.flexmark.Extension;
import com.vladsch.flexmark.ext.zzzzzz.internal.*;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html.renderer.LinkStatus;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.KeepType;
import com.vladsch.flexmark.util.collection.DataValueFactory;
import com.vladsch.flexmark.util.options.DataHolder;
import com.vladsch.flexmark.util.options.DataKey;
import com.vladsch.flexmark.util.options.MutableDataHolder;

/**
 * Extension for zzzzzzs
 * <p>
 * Create it with {@link #create()} and then configure it on the builders
 * ({@link com.vladsch.flexmark.parser.Parser.Builder#extensions(Iterable)},
 * {@link com.vladsch.flexmark.html.HtmlRenderer.Builder#extensions(Iterable)}).
 * </p>
 * <p>
 * The parsed zzzzzz text is turned into {@link Zzzzzz} nodes.
 * </p>
 */
public class ZzzzzzExtension implements Parser.ParserExtension
        , HtmlRenderer.HtmlRendererExtension // zzzoptionszzz(NODE_RENDERER, PHASED_NODE_RENDERER)
        , Parser.ReferenceHoldingExtension //zzzoptionszzz(CUSTOM_NODE_REPOSITORY)
{
    public static final DataKey<KeepType> ZZZZZZS_KEEP = new DataKey<KeepType>("ZZZZZZS_KEEP", KeepType.FIRST); //zzzoptionszzz(CUSTOM_NODE_REPOSITORY) standard option to allow control over how to handle duplicates
    public static final DataKey<ZzzzzzRepository> ZZZZZZS = new DataKey<ZzzzzzRepository>("ZZZZZZS", new DataValueFactory<ZzzzzzRepository>() { @Override public ZzzzzzRepository create(DataHolder options) { return new ZzzzzzRepository(options); } }); //zzzoptionszzz(CUSTOM_NODE_REPOSITORY)
    public static final DataKey<Boolean> ZZZZZZ_OPTION1 = new DataKey<Boolean>("ZZZZZZ_OPTION1", false); //zzzoptionszzz(CUSTOM_PROPERTIES)
    public static final DataKey<String> ZZZZZZ_OPTION2 = new DataKey<String>("ZZZZZZ_OPTION2", "default"); //zzzoptionszzz(CUSTOM_PROPERTIES)
    public static final DataKey<Integer> ZZZZZZ_OPTION3 = new DataKey<Integer>("ZZZZZZ_OPTION3", Integer.MAX_VALUE); //zzzoptionszzz(CUSTOM_PROPERTIES)
    public static final DataKey<String> LOCAL_ONLY_TARGET_CLASS = new DataKey<String>("LOCAL_ONLY_TARGET_CLASS", "local-only");//zzzoptionszzz(LINK_RESOLVER, ATTRIBUTE_PROVIDER)
    public static final DataKey<String> MISSING_TARGET_CLASS = new DataKey<String>("MISSING_TARGET_CLASS", "absent");//zzzoptionszzz(LINK_RESOLVER, ATTRIBUTE_PROVIDER)
    public static final LinkStatus LOCAL_ONLY = new LinkStatus("LOCAL_ONLY");

    private ZzzzzzExtension() {
    }

    public static Extension create() {
        return new ZzzzzzExtension();
    }

    @Override
    public void rendererOptions(final MutableDataHolder options) {

    }

    @Override
    public void parserOptions(final MutableDataHolder options) {

    }

    @Override
    public boolean transferReferences(final MutableDataHolder document, final DataHolder included) {
        if (document.contains(ZZZZZZS) && included.contains(ZZZZZZS)) {
            return Parser.transferReferences(ZZZZZZS.getFrom(document), ZZZZZZS.getFrom(included), ZZZZZZS_KEEP.getFrom(document) == KeepType.FIRST);
        }
        return false;
    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        // zzzoptionszzz(REMOVE, BLOCK_PARSER)
        // zzzoptionszzz(BLOCK_PRE_PROCESSOR)
        // zzzoptionszzz(REMOVE, DELIMITER_PROCESSOR)
        // zzzoptionszzz(REMOVE, LINK_REF_PROCESSOR)
        // zzzoptionszzz(NODE_RENDERER)
        // zzzoptionszzz(LINK_RESOLVER)
        // zzzoptionszzz(CUSTOM_PROPERTIES)
        // zzzoptionszzz(PARAGRAPH_PRE_PROCESSOR)
        // zzzoptionszzz(DOCUMENT_POST_PROCESSOR)
        // zzzoptionszzz(NODE_POST_PROCESSOR)
        // zzzoptionszzz(CUSTOM_NODE_REPOSITORY)
        // zzzoptionszzz(CUSTOM_NODE)
        // zzzoptionszzz(CUSTOM_BLOCK_NODE)
        parserBuilder.customBlockParserFactory(new ZzzzzzBlockParser.Factory());//zzzoptionszzz(REMOVE, BLOCK_PARSER)
        parserBuilder.paragraphPreProcessorFactory(ZzzzzzParagraphPreProcessor.Factory());//zzzoptionszzz(REMOVE, PARAGRAPH_PRE_PROCESSOR)
        parserBuilder.blockPreProcessorFactory(new ZzzzzzBlockPreProcessor.Factory());//zzzoptionszzz(REMOVE, BLOCK_PRE_PROCESSOR)
        parserBuilder.customDelimiterProcessor(new ZzzzzzDelimiterProcessor());//zzzoptionszzz(REMOVE, DELIMITER_PROCESSOR)
        parserBuilder.linkRefProcessorFactory(new ZzzzzzLinkRefProcessor.Factory());//zzzoptionszzz(REMOVE, LINK_REF_PROCESSOR)
        parserBuilder.postProcessorFactory(new ZzzzzzNodePostProcessor.Factory());//zzzoptionszzz(REMOVE, NODE_POST_PROCESSOR)
        parserBuilder.postProcessorFactory(new ZzzzzzDocumentPostProcessor.Factory());//zzzoptionszzz(REMOVE, DOCUMENT_POST_PROCESSOR)
        parserBuilder.customInlineParserExtensionFactory(new ZzzzzzInlineParserExtension.Factory());//zzzoptionszzz(REMOVE, INLINE_PARSER_EXTENSION)
    }

    @Override
    public void extend(HtmlRenderer.Builder rendererBuilder, String rendererType) {
        if (rendererBuilder.isRendererType("HTML")) {
            rendererBuilder.nodeRendererFactory(new ZzzzzzNodeRenderer.Factory());// zzzoptionszzz(NODE_RENDERER, PHASED_NODE_RENDERER)
            rendererBuilder.linkResolverFactory(new ZzzzzzLinkResolver.Factory());// zzzoptionszzz(LINK_RESOLVER, NODE_RENDERER, PHASED_NODE_RENDERER)
            rendererBuilder.attributeProviderFactory(new ZzzzzzAttributeProvider.Factory());// zzzoptionszzz(ATTRIBUTE_PROVIDER, NODE_RENDERER, PHASED_NODE_RENDERER)
        } else if (rendererBuilder.isRendererType("JIRA")) {
            rendererBuilder.nodeRendererFactory(new ZzzzzzJiraRenderer.Factory());// zzzoptionszzz(NODE_RENDERER, PHASED_NODE_RENDERER)
            rendererBuilder.linkResolverFactory(new ZzzzzzLinkResolver.Factory());// zzzoptionszzz(LINK_RESOLVER, NODE_RENDERER, PHASED_NODE_RENDERER)
            rendererBuilder.attributeProviderFactory(new ZzzzzzAttributeProvider.Factory());// zzzoptionszzz(ATTRIBUTE_PROVIDER, NODE_RENDERER, PHASED_NODE_RENDERER)
        }
    }
}
