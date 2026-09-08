# java-xml-xpath-with-namesapces

## Requirements

- Build a small Java application that loads XML from a file or an input stream.
- Provide methods to evaluate XPath expressions against the loaded document.
- Automatically resolve XML namespaces when evaluating XPath expressions.
- Structure the application with two classes:
	- `Main`: demonstrates sample usage and includes example invocation methods.
	- `XmlUtils`: a utility class exposing static methods to load XML, evaluate XPath expressions, and return results in a generic form.
- Include sample XML input and usage examples demonstrating namespace-aware XPath evaluation.

This project provides a concise, namespace-aware XPath evaluation example suitable for reuse and testing.

## VS Code: Run Maven from the integrated terminal

1. Install Apache Maven on Windows (one easy option is Chocolatey):

```powershell
choco install maven -y
```

2. Verify installation in a new terminal:

```powershell
mvn -v
```

3. If `mvn` is not on your PATH, either add the `bin` folder of your Maven installation to the system `PATH`, or edit the workspace VS Code setting at `.vscode/settings.json` and replace `C:\\path\\to\\apache-maven\\bin` with your Maven `bin` path.

4. Run the packaged task from the Command Palette (Ctrl+Shift+P) → `Tasks: Run Task` → `Maven: Package`, or run `mvn package` directly in the integrated terminal.

Note: On Windows you can also use the included task which invokes `mvn` in the workspace root.

##  Development environment 

*   JAVA_HOME=F:\java\openlogic-openjdk-17.0.18+8-windows-x64
*   MVN_HOME=F:\java\apache-maven-3.9.13
*   Java to target JDK 11
*   Use JDK as far as possible with as few maven dependencies as possible