package org.elasticsearch.index.query.image;

import net.semanticmetadata.lire.imageanalysis.LireFeature;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.util.ToStringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * Copied from {@link MatchAllDocsQuery}, calculate score for all docs
 */
public class ImageQuery extends Query {

    private String luceneFieldName;
    private LireFeature lireFeature;

    public ImageQuery(String luceneFieldName, LireFeature lireFeature, float boost) {
        this.luceneFieldName = luceneFieldName;
        this.lireFeature = lireFeature;
        setBoost(boost);
    }

    private class ImageScorer extends AbstractImageScorer {
        private int doc = -1;
        private final int maxDoc;

        ImageScorer(IndexReader reader, Weight w) {
            super(w, luceneFieldName, lireFeature, reader, ImageQuery.this.getBoost());
            maxDoc = reader.maxDoc();
        }

        @Override
        public int docID() {
            return doc;
        }

        @Override
        public int nextDoc() throws IOException {
            doc++;
            while(doc < maxDoc) {
                doc++;
            }
            if (doc == maxDoc) {
                doc = NO_MORE_DOCS;
            }
            return doc;
        }


        @Override
        public int advance(int target) throws IOException {
            doc = target-1;
            return nextDoc();
        }

        @Override
        public long cost() {
            return maxDoc;
        }
    }

    private class ImageWeight extends Weight {
        protected ImageWeight(Query query) {
            super(query);
        }

        @Override
        public String toString() {
            return "weight(" + ImageQuery.this + ")";
        }

        @Override
        public float getValueForNormalization() {
            return 1f;
        }

        @Override
        public void normalize(float queryNorm, float topLevelBoost) {
        }

        @Override
        public Scorer scorer(LeafReaderContext context) throws IOException {
            return new ImageScorer(context.reader(), this);
        }

        @Override
        public Explanation explain(LeafReaderContext context, int doc) throws IOException {
            Scorer scorer = scorer(context);
            boolean exists = (scorer != null && scorer.advance(doc) == doc);
            if(exists){
                float score = scorer.score();
                List<Explanation> details=new ArrayList<>();
                if (getBoost() != 1.0f) {
                    details.add(Explanation.match(getBoost(), "boost"));
                    score = score / getBoost();
                }
                details.add(Explanation.match(score ,"image score (1/distance)"));
                return Explanation.match(
                        score, ImageQuery.this.toString() + ", product of:",details);
            }else{
                return Explanation.noMatch(ImageQuery.this.toString() + " doesn't match id " + doc);
            }
        }

        @Override
        public void extractTerms(Set<Term> terms) {
        }
    }

    @Override
    public Weight createWeight(IndexSearcher searcher, boolean needsScores) {
        return new ImageWeight(this);
    }

    @Override
    public String toString(String field) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(luceneFieldName);
        buffer.append(",");
        buffer.append(lireFeature.getClass().getSimpleName());
        buffer.append(ToStringUtils.boost(getBoost()));
        return buffer.toString();
    }


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ImageQuery))
            return false;
        ImageQuery other = (ImageQuery) o;
        return (this.getBoost() == other.getBoost())
                && luceneFieldName.equals(luceneFieldName)
                && lireFeature.equals(lireFeature);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + luceneFieldName.hashCode();
        result = 31 * result + lireFeature.hashCode();
        result = Float.floatToIntBits(getBoost()) ^ result;
        return result;
    }


}
