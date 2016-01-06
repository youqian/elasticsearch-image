package org.elasticsearch.plugin.image;

import org.elasticsearch.common.inject.Module;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.mapper.image.ImageMapper;
import org.elasticsearch.index.query.image.ImageQueryParser;
import org.elasticsearch.indices.IndicesModule;
import org.elasticsearch.plugins.Plugin;

import java.util.Collection;
import java.util.Collections;


public class ImagePlugin extends Plugin {

    @Override
    public String name() {
        return "image";
    }

    @Override
    public String description() {
        return "Elasticsearch Image Plugin";
    }

    public void onModule(IndicesModule indicesModule) {
        indicesModule.registerMapper("image", new ImageMapper.TypeParser());
        indicesModule.registerQueryParser(ImageQueryParser.class);
    }
}
