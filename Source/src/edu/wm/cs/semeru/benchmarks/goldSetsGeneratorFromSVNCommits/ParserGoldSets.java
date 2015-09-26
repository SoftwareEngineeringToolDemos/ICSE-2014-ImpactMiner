package edu.wm.cs.semeru.benchmarks.goldSetsGeneratorFromSVNCommits;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class ParserGoldSets
{
	private String fileContent;
	private String packageName;
	private InputOutputGoldSetsGeneratorFromSVNCommits inputOutput;
	
	public ParserGoldSets(InputOutputGoldSetsGeneratorFromSVNCommits inputOutput,String fileContent)
	{
		this.inputOutput=inputOutput;
		this.fileContent=fileContent;
		this.packageName="";
	}

	public CompilationUnit parseSourceCode()
	{
		char[] fileContentAsChar=fileContent.toCharArray();
		ASTParser parser=ASTParser.newParser(AST.JLS4);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(fileContentAsChar);
		
		return (CompilationUnit)parser.createAST(null);
	}
	
	public ArrayList<CorpusMethod> exploreSourceCodeAndIgnoreComments(CompilationUnit compilationUnitSourceCode) throws Exception
	{
		if (compilationUnitSourceCode.getPackage()!=null)
			packageName=compilationUnitSourceCode.getPackage().getName().toString();
		
		List<ASTNode> declaredTypes=compilationUnitSourceCode.types();
		
		ArrayList<CorpusMethod> listOfCorpusMethods=new ArrayList<CorpusMethod>();

		for (ASTNode currentDeclaredType:declaredTypes)
		{
			//if node is a class
			if (currentDeclaredType.getNodeType()==ASTNode.TYPE_DECLARATION)
			{
				exploreClassContentsAndIgnoreComments((TypeDeclaration)currentDeclaredType,"",listOfCorpusMethods);
			}
		}

		return listOfCorpusMethods;
	}
	
	private void exploreClassContentsAndIgnoreComments(TypeDeclaration classNode,String prefixClass,ArrayList<CorpusMethod> listOfCorpusMethods) throws Exception
	{
		List<ASTNode> bodyDeclarations=classNode.bodyDeclarations();

		SimpleName className=classNode.getName();
		
		String fullClassName=prefixClass+className+".";

		for (ASTNode bodyDeclaration:bodyDeclarations)
		{
			//explore method contents 
			if (bodyDeclaration.getNodeType()==ASTNode.METHOD_DECLARATION)
			{
				exploreMethodContentsAndIgnoreComments((MethodDeclaration)bodyDeclaration,fullClassName,listOfCorpusMethods);
			}

			//recursively explore inner classes
			if (bodyDeclaration.getNodeType()==ASTNode.TYPE_DECLARATION)
			{
				exploreClassContentsAndIgnoreComments((TypeDeclaration)bodyDeclaration,fullClassName,listOfCorpusMethods);
			}
		}
	}
	
	private void exploreMethodContentsAndIgnoreComments(MethodDeclaration methodDeclaration, String fullClassName, ArrayList<CorpusMethod> listOfCorpusMethods) throws Exception
	{
		String currentMethodName=methodDeclaration.getName().getFullyQualifiedName();
		String idMethod=fullClassName+currentMethodName;

		//ignore a method declared inside an interface
		//this allows for methods declared inside classes declared inside interfaces to be indexed
		TypeDeclaration parentOfMethod=(TypeDeclaration)methodDeclaration.getParent();
		if (parentOfMethod.isInterface())
			return;
		
		//detect abstract methods and ignore them
		if (Modifier.isAbstract(methodDeclaration.getModifiers()))
		{
			System.out.println("ID="+idMethod);
			System.err.println("Abstract method (ignored) "+"ID="+idMethod);
			return;
		}
		                                          
		String idMethodDebug=idMethod;
		
		List<SingleVariableDeclaration> listOfParameters=methodDeclaration.parameters();
		idMethodDebug+="\t"+listOfParameters.size()+"\t";
		
		for (SingleVariableDeclaration parameter:listOfParameters)
		{
			idMethodDebug+=parameter.getType()+"\t";
		}
		
		idMethod=convertMethodToIncludeParameters(idMethodDebug);

		String methodContents=methodDeclaration.getBody().toString();
		
		idMethod=packageName+"."+idMethod;
		idMethodDebug=packageName+"."+idMethodDebug;
		
		inputOutput.appendToGoldSetFileDebug(idMethodDebug);
		
		listOfCorpusMethods.add(new CorpusMethod(idMethod,methodContents));
	}
	
	private String convertMethodToIncludeParameters(String methodID)
	{
		String[] splittedMethodID=methodID.split("\t");
		String buf=splittedMethodID[0]+"(";
		int numberOfParameters=Integer.parseInt(splittedMethodID[1]);

		if (numberOfParameters>=1)
		{
			buf+=splittedMethodID[2];
		}
		for (int indexParameter=2;indexParameter<=numberOfParameters;indexParameter++)
		{
			buf+=","+splittedMethodID[indexParameter+1];
		}
		buf+=")";
		return buf;
	}
}
