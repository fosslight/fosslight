declare global {
  namespace Common {
    type OSSTypes = Record<string, { name: string; desc: string; color: string }>;
  }

  namespace Nav {
    interface RootMenu {
      name: string;
      icon: string;
    }

    interface Menu {
      name: string;
      icon: string;
      path: string;
      sub?: {
        name: string;
        path: string;
      }[];
    }
  }

  namespace List {
    interface Filter {
      label: React.ReactNode;
      name: string;
      type: 'char' | 'char-exact' | 'select' | 'checkbox' | 'date' | 'number' | 'text';
      options?: { label: React.ReactNode; value: string }[];
    }

    interface Column {
      name: string;
      sort: string;
    }

    interface SelfCheck {
      projectId: string;
      projectName: string;
      projectVersion: string;
      report: string;
      packages: string[];
      notice: string;
      cveId: string;
      cvssScore: string;
      created: string;
    }

    interface OSS {
      ossId: string;
      ossName: string;
      ossVersion: string;
      ossType: string;
      licenseName: string;
      licenseType: string;
      obligations: string[];
      downloadUrl: string;
      homepageUrl: string;
      description: string;
      cveId: string;
      cvssScore: string;
      creator: string;
      created: string;
      modifier: string;
      modified: string;
    }

    interface License {
      licenseId: string;
      licenseName: string;
      licenseIdentifier: string;
      licenseType: string;
      obligations: string[];
      restrictions: string[];
      homepageUrl: string;
      description: string;
      creator: string;
      created: string;
      modifier: string;
      modified: string;
    }

    interface Vuln {
      ossName: string;
      ossVersion: string;
      vendor: string;
      cveId: string;
      cvssScore: string;
      summary: string;
      modified: string;
    }
  }

  namespace SelfCheck {
    interface Edit {
      projectId: string;
      projectName: string;
      projectVersion: string;
      comment: string;
    }

    interface Tab {
      name: 'OSS' | 'Package' | 'Notice';
      title: string;
      description: string;
    }

    interface OSSFile {
      name: string;
      when: string;
    }

    interface OSSLicense {
      licenseId: string;
      licenseIdentifier: string;
    }

    interface OSS {
      path: string;
      ossId: string;
      ossName: string;
      ossVersion: string;
      licenses: OSSLicense[];
      obligations: string[];
      restrictions: string[];
      downloadUrl: string;
      homepageUrl: string;
      description: string;
      copyright: string;
      cveId: string;
      cvssScore: string;
      exclude: boolean;
    }

    interface EditOSS {
      path: string;
      ossName: string;
      ossVersion: string;
      licenses: OSSLicense[];
      downloadUrl: string;
      homepageUrl: string;
      copyright: string;
    }

    interface OSSCheck {
      downloadUrl: string;
      ossName: string;
      newOssName: string;
    }

    interface LicenseCheck {
      ossName: string;
      ossVersion: string;
      downloadUrl: string;
      licenses: string[];
      newLicenses: string[];
    }

    interface PackageFile {
      name: string;
      when: string;
    }

    interface PackageOSS {
      ossId: string;
      ossName: string;
      ossVersion: string;
      licenseName: string;
      downloadUrl: string;
      homepageUrl: string;
    }
  }

  namespace ListSection {
    interface Vuln {
      ossName: string;
      ossVersion: string;
      cveId: string;
      cvssScore: string;
      summary: string;
      modified: string;
    }

    interface OSS {
      ossId: string;
      ossName: string;
      ossVersion: string;
      licenseName: string;
      obligations: string[];
      cveId: string;
      cvssScore: string;
      created: string;
      modified: string;
    }

    interface License {
      licenseId: string;
      licenseName: string;
      licenseIdentifier: string;
      obligations: string[];
      restrictions: string[];
      created: string;
      modified: string;
    }
  }

  namespace Detail {
    interface OSSLicense {
      licenseId: string;
      licenseName: string;
      licenseIdentifier: string;
      comb: '' | 'AND' | 'OR';
    }

    interface OSSVuln {
      cveId: string;
      cvssScore: string;
      summary: string;
    }

    interface OSS {
      ossName: string;
      ossNicknames: string[];
      ossVersion: string;
      ossType: string;
      licenses: OSSLicense[];
      licenseType: string;
      obligations: string[];
      downloadUrl: string;
      homepageUrl: string;
      description: string;
      copyright: string;
      attribution: string;
      vulnerabilities: OSSVuln[];
      deactivate: boolean;
      creator: string;
      created: string;
      modifier: string;
      modified: string;
    }

    interface License {
      licenseName: string;
      licenseNicknames: string[];
      licenseIdentifier: string;
      licenseType: string;
      obligations: string[];
      restrictions: string[];
      homepageUrl: string;
      description: string;
      licenseText: string;
      attribution: string;
      creator: string;
      created: string;
      modifier: string;
      modified: string;
    }

    interface VulnOSS {
      ossId: string;
      ossName: string;
      ossVersion: string;
    }

    interface Vuln {
      cveId: string;
      cvssScore: string;
      summary: string;
      modified: string;
      oss: VulnOSS[];
    }
  }
}

export {};
