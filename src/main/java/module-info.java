module it.univaq.brewhub {
    requires transitive javafx.controls;
    requires transitive java.sql;
    requires jbcrypt;
	requires javafx.graphics;
	requires javafx.media;
	requires javafx.base;
    
    opens it.univaq.brewhub;
    opens it.univaq.brewhub.UI;
    
    exports it.univaq.brewhub;
    exports it.univaq.brewhub.UI;
}
