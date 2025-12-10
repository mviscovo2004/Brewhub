module it.univaq.brewhub {
    requires javafx.controls;
    requires jsondb.core;
    requires com.fasterxml.jackson.core;
    requires jbcrypt;
	requires com.fasterxml.jackson.annotation;
	requires javafx.graphics;
    
    opens it.univaq.brewhub;
    
    exports it.univaq.brewhub;
}
