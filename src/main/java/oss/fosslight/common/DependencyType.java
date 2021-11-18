package oss.fosslight.common;

import java.util.Arrays;
import java.util.regex.Pattern;

/* dependency type enum */
public enum DependencyType {
	GITHUB("git", "github", CoConstDef.GITHUB_PATTERN),
	NPM("npm", "npmjs", CoConstDef.NPM_PATTERN),
	PYPI("pypi", "pypi", CoConstDef.PYPI_PATTERN),
	MAVEN_CENTRAL("maven", "mavencentral", CoConstDef.MAVEN_CENTRAL_PATTERN),
	MAVEN_GOOGLE("maven", "mavengoogle", CoConstDef.MAVEN_GOOGLE_PATTERN),
	COCOAPODS("pod", "cocoapods", CoConstDef.COCOAPODS_PATTERN),
	UNSUPPORTED("unsupported", "unsupported", CoConstDef.UNSUPPORTED_PATTERN);

	String type;
	String provider;
	private Pattern pattern;

	DependencyType(String type, String provider, Pattern pattern) {
		this.type = type;
		this.provider = provider;
		this.pattern = pattern;
	}

	public String getType() {
		return type;
	}

	public String getProvider() {
		return provider;
	}

	public Pattern getPattern() {
		return pattern;
	}

	public static DependencyType downloadLocationToType(String downloadLocation) {
		return Arrays.stream(DependencyType.values())
			.filter(dependency -> {
				Pattern pattern = dependency.getPattern();
				return pattern.matcher(downloadLocation).matches();
			})
			.findAny()
			.orElse(DependencyType.UNSUPPORTED);
	}
}