package com.talos.viam.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.resource.ResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;


@Configuration
public class WebConfig extends WebMvcConfigurerAdapter
{
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/govianex","/govianex/**")
				.addResourceLocations("classpath:/static/govianex/")
				.resourceChain(false)
				.addResolver(new PushStateResourceResolver("govianex"));

		registry.addResourceHandler("/plaid","/plaid/**")
				.addResourceLocations("classpath:/static/plaid/")
				.resourceChain(false)
				.addResolver(new PushStateResourceResolver("plaid"));
	}

	private class PushStateResourceResolver implements ResourceResolver
	{
		private Resource index;
		private List<String> handledExtensions = Arrays
				.asList("html", "js", "json", "csv", "css", "png", "svg", "eot", "ttf", "woff", "appcache", "jpg", "jpeg", "gif", "ico");
		private List<String> ignoredPaths = Arrays.asList("api");

		public PushStateResourceResolver(String prefix){
			index = new ClassPathResource("/static/"+prefix+"/index.html");
		}

		@Override
		public Resource resolveResource(HttpServletRequest request, String requestPath, List<? extends Resource> locations, ResourceResolverChain chain) {
			return resolve(requestPath, locations);
		}

		@Override
		public String resolveUrlPath(String resourcePath, List<? extends Resource> locations, ResourceResolverChain chain) {
			Resource resolvedResource = resolve(resourcePath, locations);
			if (resolvedResource == null) {
				return null;
			}
			try {
				return resolvedResource.getURL().toString();
			} catch (IOException e) {
				return resolvedResource.getFilename();
			}
		}

		private Resource resolve(String requestPath, List<? extends Resource> locations) {
			if (isIgnored(requestPath)) {
				return null;
			}
			if (isHandled(requestPath)) {
				return locations.stream()
						.map(loc -> createRelative(loc, requestPath))
						.filter(resource -> resource != null && resource.exists())
						.findFirst()
						.orElseGet(null);
			}
			return index;
		}

		private Resource createRelative(Resource resource, String relativePath) {
			try {
				return resource.createRelative(relativePath);
			} catch (IOException e) {
				return null;
			}
		}

		private boolean isIgnored(String path) {
			return ignoredPaths.contains(path);
		}

		private boolean isHandled(String path) {
			String extension = StringUtils.getFilenameExtension(path);
			return handledExtensions.stream().anyMatch(ext -> ext.equals(extension));
		}
	}
}
