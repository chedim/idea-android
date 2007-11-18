package org.jetbrains.android.dom.converters;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.CustomReferenceConverter;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.ResolvingConverter;
import org.jetbrains.android.dom.resources.ResourceElement;
import org.jetbrains.android.dom.resources.ResourceValue;
import org.jetbrains.android.dom.ResourceType;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author yole
 */
public class ResourceReferenceConverter extends ResolvingConverter<ResourceValue> implements CustomReferenceConverter<ResourceValue> {
    @NotNull
    public Collection<? extends ResourceValue> getVariants(ConvertContext context) {
        List<ResourceValue> result = new ArrayList<ResourceValue>();
        ResourceType resourceType = context.getInvocationElement().getAnnotation(ResourceType.class);
        AndroidFacet facet = AndroidFacet.getInstance(context.getModule());
        if (facet != null && resourceType != null) {
            List<ResourceElement> elements = facet.getResourcesOfType(resourceType.value());
            for(ResourceElement element: elements) {
                String name = element.getName().getValue();
                if (name != null) {
                    result.add(ResourceValue.referenceTo('@', resourceType.value(), name));
                }
            }
        }
        return result;
    }

    public ResourceValue fromString(@Nullable @NonNls String s, ConvertContext context) {
        return ResourceValue.parse(s);
    }

    public String toString(@Nullable ResourceValue resourceElement, ConvertContext context) {
        return resourceElement != null ? resourceElement.toString() : null;
    }

    @NotNull
    public PsiReference[] createReferences(GenericDomValue<ResourceValue> value, PsiElement element, ConvertContext context) {
        ResourceValue ref = value.getValue();
        if (ref != null && ref.isReference()) {
            String resType = ref.getResourceType();
            AndroidFacet facet = AndroidFacet.getInstance(context.getModule());
            GenericDomValue target = null;
            List<ResourceElement> list = facet.getResourcesOfType(resType);
            for(ResourceElement rs: list) {
                if (ref.getResourceName().equals(rs.getName().getValue())) {
                    target = rs.getName();
                }
            }
            return new PsiReference[] { new ResourceReference(value, target)};
        }
        return new PsiReference[0];
    }
}