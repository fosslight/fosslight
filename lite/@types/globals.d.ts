declare global {
  interface Filter {
    label: React.ReactNode;
    name: string;
    type: 'char' | 'char-exact' | 'select' | 'checkbox' | 'date' | 'number' | 'text';
    options?: { label: React.ReactNode; value: string }[];
  }

  interface SelfCheckTab {
    name: 'OSS' | 'Package' | 'Notice';
    title: string;
    description: string;
  }

  interface OSSLicense {
    id: string;
    name: string;
    identifier: string;
    comb: '' | 'AND' | 'OR';
  }

  interface OSSVuln {
    id: string;
    score: string;
    summary: string;
  }

  interface VulnOSS {
    id: string;
    name: string;
    ver: string;
  }
}

export {};
