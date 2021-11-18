package oss.fosslight.common;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.util.UriComponentsBuilder;

/* External API support dependency type (GitHub, Clearly Defined) */
public enum ExternalServiceType {
	GITHUB(
			"https",
			"api.github.com",
			"/repos/{onwer}/{repo}/license",
			"github license api",
			Arrays.asList(
					DependencyType.GITHUB
			)
	),
	CLEARLY_DEFINED(
			"https",
			"api.clearlydefined.io",
			"/definitions/{type}/{provider}/{namespace}/{name}/{revision}",
			"Clearly Defined definitions api",
			Arrays.asList(
					DependencyType.NPM,
					DependencyType.MAVEN_CENTRAL,
					DependencyType.MAVEN_GOOGLE,
					DependencyType.PYPI,
					DependencyType.COCOAPODS
			)
	);

	private String schema;
	private String host;
	private String path;
	private String description;
	private List<DependencyType> types;

	ExternalServiceType(String schema, String host, String path, String description, List<DependencyType> types) {
		this.schema = schema;
		this.host = host;
		this.path = path;
		this.description = description;
		this.types = types;
	}

	public List<DependencyType> getTypes() {
		return types;
	}

	public String getDescription() {
		return description;
	}

	public String getPath() {
		return path;
	}

	public String getSchema() {
		return schema;
	}

	public String getHost() {
		return host;
	}

	public boolean hasDependencyType(DependencyType type) {
		return types.stream()
				.anyMatch(dependencyType -> dependencyType.equals(type));
	}

	public static String githubLicenseRequestUri(String owner, String repo) {
		return UriComponentsBuilder.newInstance()
				.scheme(GITHUB.getSchema())
				.host(GITHUB.getHost())
				.path(GITHUB.getPath())
				.build()
				.expand(owner, repo)
				.encode().toUriString();
	}

	public static String clearlyDefinedLicenseRequestUri(String type, String provider, String namespace, String name, String revision) {
		return UriComponentsBuilder.newInstance()
				.scheme(CLEARLY_DEFINED.getSchema())
				.host(CLEARLY_DEFINED.getHost())
				.path(CLEARLY_DEFINED.getPath())
				.build()
				.expand(type, provider, namespace, name, revision)
				.encode().toUriString();
	}
}