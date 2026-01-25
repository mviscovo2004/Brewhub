@SuppressWarnings("module") module it.univaq.brewhub {
    requires transitive javafx.controls;
    requires transitive java.sql;
    requires jbcrypt;
    requires javafx.graphics;
    requires javafx.media;
    requires javafx.base;

    opens it.univaq.brewhub;
    opens it.univaq.brewhub.UI;
    opens it.univaq.brewhub.UI.components;
    opens it.univaq.brewhub.business;
    opens it.univaq.brewhub.dao;
    opens it.univaq.brewhub.dao.impl;
    opens it.univaq.brewhub.utility;

    exports it.univaq.brewhub;
    exports it.univaq.brewhub.UI;
    exports it.univaq.brewhub.UI.components;
    exports it.univaq.brewhub.business;
    exports it.univaq.brewhub.dao;
    exports it.univaq.brewhub.dao.impl;
    exports it.univaq.brewhub.utility;
    exports it.univaq.brewhub.model;

    opens it.univaq.brewhub.model;
}
