package taintanalysis.sourcesink.definitions;


import soot.jimple.infoflow.sourcesSinks.definitions.*;
import soot.tagkit.AnnotationTag;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * A source/sink definition corresponds to parameters with certain tags
 *
 */
public class ParamSourceSinkDefinition extends AbstractSourceSinkDefinition {

    protected final AnnotationTag tag;
    protected Set<AccessPathTuple> accessPaths;

    public ParamSourceSinkDefinition(AnnotationTag tag) {
        this(tag, null);
    }

    public ParamSourceSinkDefinition(AnnotationTag tag, Set<AccessPathTuple> accessPaths) {
        this.tag = tag;
        this.accessPaths = accessPaths;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ISourceSinkDefinition getSourceOnlyDefinition() {
        Set<AccessPathTuple> newSet = null;
        if (accessPaths != null) {
            newSet = accessPaths.stream()
                    .filter(ap -> ap.getSourceSinkType().isSource())
                    .collect(Collectors.toSet());

        }
        return new ParamSourceSinkDefinition(tag, newSet);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ISourceSinkDefinition getSinkOnlyDefinition() {
        Set<AccessPathTuple> newSet = null;
        if (accessPaths != null) {
            newSet = accessPaths.stream()
                    .filter(ap -> ap.getSourceSinkType().isSink())
                    .collect(Collectors.toSet());

        }
        return new ParamSourceSinkDefinition(tag, newSet);
    }

    @Override
    public void merge(ISourceSinkDefinition iSourceSinkDefinition) {

    }

    @Override
    public boolean isEmpty() {
        return tag != null;
    }
    @Override
    public String toString() { return this.getTagType();}

    public String getTagType() {
        return tag == null ? "Null tag" : tag.getType().substring(1)
                .replace('/', '.')
                .replace(";","");
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((tag == null) ? 0 : tag.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        ParamSourceSinkDefinition other = (ParamSourceSinkDefinition) obj;
        if (accessPaths == null) {
            return other.accessPaths == null;
        } else {
            return accessPaths.equals(other.accessPaths);
        }
    }
}
