# Makefile untuk Rush Hour Puzzle GUI (macOS dan Windows)

# JAVAFX_LIB = lib
JAVAFX_LIB = $(HOME)/javafx/javafx-sdk-21.0.7/lib # Ganti dengan path ke JavaFX SDK yang dimiliki
JAVAFX_MODULES = javafx.controls,javafx.fxml,javafx.media
JAVA_PACKAGE = src
BIN_DIR = bin
MAIN_CLASS = gui.App

# Deteksi OS
ifeq ($(OS),Windows_NT)
    # Windows settings
    PATHSEP = ;
    RM = rmdir /s /q
    MKDIR = if not exist $(subst /,\,$1) mkdir $(subst /,\,$1)
    CP = copy /Y
    FIND_JARS = $(subst /,\,$(JAVAFX_LIB))\*.jar
    CLASSPATH = $(BIN_DIR)$(PATHSEP)$(subst /,\,$(JAVAFX_LIB))\*.jar
else
    # macOS/Unix settings
    PATHSEP = :
    RM = rm -rf
    MKDIR = mkdir -p $1
    CP = cp -f
    FIND_JARS = $(shell find $(JAVAFX_LIB) -name "*.jar" | tr '\n' '$(PATHSEP)')
    CLASSPATH = $(BIN_DIR)$(PATHSEP)$(FIND_JARS)
endif

# Java commands
JAVAC = javac
JAVA = java

.PHONY: all
all: compile run

.PHONY: clean
clean:
ifeq ($(OS),Windows_NT)
	$(RM) $(subst /,\,$(BIN_DIR))
else
	$(RM) $(BIN_DIR)
endif

.PHONY: compile
compile: init
ifeq ($(OS),Windows_NT)
	$(JAVAC) -d $(BIN_DIR) -cp "$(JAVA_PACKAGE)$(PATHSEP)$(FIND_JARS)" --module-path $(JAVAFX_LIB) --add-modules $(JAVAFX_MODULES) $(JAVA_PACKAGE)/gui/*.java
	$(CP) $(subst /,\,$(JAVA_PACKAGE)\gui\*.css) $(subst /,\,$(BIN_DIR)\gui\) > NUL 2>&1 || (exit 0)
else
	$(JAVAC) -d $(BIN_DIR) -cp $(JAVA_PACKAGE)$(PATHSEP)$(FIND_JARS) --module-path $(JAVAFX_LIB) --add-modules $(JAVAFX_MODULES) $(JAVA_PACKAGE)/gui/*.java
	$(CP) $(JAVA_PACKAGE)/gui/*.css $(BIN_DIR)/gui/ 2>/dev/null || :
endif

.PHONY: run
run:
ifeq ($(OS),Windows_NT)
	$(JAVA) --module-path $(JAVAFX_LIB) --add-modules $(JAVAFX_MODULES) -cp "$(CLASSPATH)" $(MAIN_CLASS)
else
	$(JAVA) --module-path $(JAVAFX_LIB) --add-modules $(JAVAFX_MODULES) -cp $(CLASSPATH) $(MAIN_CLASS)
endif

.PHONY: init
init:
ifeq ($(OS),Windows_NT)
	$(call MKDIR,$(subst /,\,$(BIN_DIR)))
	$(call MKDIR,$(subst /,\,$(BIN_DIR)\gui))
else
	$(call MKDIR,$(BIN_DIR))
	$(call MKDIR,$(BIN_DIR)/gui)
endif