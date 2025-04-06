// Generated from C:/Users/maxis/Desktop/Compilators/Projeto/CalcCompiler-codeTutoria/src/Tuga.g4 by ANTLR 4.13.2
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class TugaLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, LPAREN=2, RPAREN=3, UMINUS=4, PLUS=5, MINUS=6, TIMES=7, DIV=8, 
		REMAINDER=9, LESS=10, GREATER=11, LESOEQ=12, GREOEQ=13, EQUAL=14, DIF=15, 
		AND=16, OR=17, NOT=18, INT=19, REAL=20, STRING=21, BOOL=22, COMMA=23, 
		SL_COMMENT=24, ML_COMMENT=25, NEWLINE=26, WS=27;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "LPAREN", "RPAREN", "UMINUS", "PLUS", "MINUS", "TIMES", "DIV", 
			"REMAINDER", "LESS", "GREATER", "LESOEQ", "GREOEQ", "EQUAL", "DIF", "AND", 
			"OR", "NOT", "INT", "REAL", "STRING", "BOOL", "COMMA", "SL_COMMENT", 
			"ML_COMMENT", "NEWLINE", "WS", "DIGIT"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'escreve'", "'('", "')'", null, "'+'", "'-'", "'*'", "'/'", "'%'", 
			"'<'", "'>'", "'<='", "'>='", "'igual'", "'diferente'", "'e'", "'ou'", 
			"'nao'", null, null, null, null, "';'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, "LPAREN", "RPAREN", "UMINUS", "PLUS", "MINUS", "TIMES", "DIV", 
			"REMAINDER", "LESS", "GREATER", "LESOEQ", "GREOEQ", "EQUAL", "DIF", "AND", 
			"OR", "NOT", "INT", "REAL", "STRING", "BOOL", "COMMA", "SL_COMMENT", 
			"ML_COMMENT", "NEWLINE", "WS"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public TugaLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Tuga.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\u0004\u0000\u001b\u00d0\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002"+
		"\u0001\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002"+
		"\u0004\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002"+
		"\u0007\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002"+
		"\u000b\u0007\u000b\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e"+
		"\u0002\u000f\u0007\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011"+
		"\u0002\u0012\u0007\u0012\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014"+
		"\u0002\u0015\u0007\u0015\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017"+
		"\u0002\u0018\u0007\u0018\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a"+
		"\u0002\u001b\u0007\u001b\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000"+
		"\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001"+
		"\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004"+
		"\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007"+
		"\u0001\b\u0001\b\u0001\t\u0001\t\u0001\n\u0001\n\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\f\u0001\f\u0001\f\u0001\r\u0001\r\u0001\r\u0001\r\u0001"+
		"\r\u0001\r\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e"+
		"\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000f"+
		"\u0001\u000f\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0011\u0001\u0011"+
		"\u0001\u0011\u0001\u0011\u0001\u0012\u0004\u0012v\b\u0012\u000b\u0012"+
		"\f\u0012w\u0001\u0013\u0004\u0013{\b\u0013\u000b\u0013\f\u0013|\u0001"+
		"\u0013\u0001\u0013\u0004\u0013\u0081\b\u0013\u000b\u0013\f\u0013\u0082"+
		"\u0001\u0014\u0001\u0014\u0005\u0014\u0087\b\u0014\n\u0014\f\u0014\u008a"+
		"\t\u0014\u0001\u0014\u0001\u0014\u0001\u0015\u0001\u0015\u0001\u0015\u0001"+
		"\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001"+
		"\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0003"+
		"\u0015\u009d\b\u0015\u0001\u0016\u0001\u0016\u0001\u0017\u0001\u0017\u0001"+
		"\u0017\u0001\u0017\u0005\u0017\u00a5\b\u0017\n\u0017\f\u0017\u00a8\t\u0017"+
		"\u0001\u0017\u0003\u0017\u00ab\b\u0017\u0001\u0017\u0001\u0017\u0001\u0018"+
		"\u0001\u0018\u0001\u0018\u0001\u0018\u0005\u0018\u00b3\b\u0018\n\u0018"+
		"\f\u0018\u00b6\t\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018"+
		"\u0005\u0018\u00bc\b\u0018\n\u0018\f\u0018\u00bf\t\u0018\u0001\u0018\u0001"+
		"\u0018\u0001\u0019\u0003\u0019\u00c4\b\u0019\u0001\u0019\u0001\u0019\u0001"+
		"\u001a\u0004\u001a\u00c9\b\u001a\u000b\u001a\f\u001a\u00ca\u0001\u001a"+
		"\u0001\u001a\u0001\u001b\u0001\u001b\u0003\u0088\u00a6\u00b4\u0000\u001c"+
		"\u0001\u0001\u0003\u0002\u0005\u0003\u0007\u0004\t\u0005\u000b\u0006\r"+
		"\u0007\u000f\b\u0011\t\u0013\n\u0015\u000b\u0017\f\u0019\r\u001b\u000e"+
		"\u001d\u000f\u001f\u0010!\u0011#\u0012%\u0013\'\u0014)\u0015+\u0016-\u0017"+
		"/\u00181\u00193\u001a5\u001b7\u0000\u0001\u0000\u0003\u0001\u0001\n\n"+
		"\u0003\u0000\t\n\r\r  \u0001\u000009\u00d8\u0000\u0001\u0001\u0000\u0000"+
		"\u0000\u0000\u0003\u0001\u0000\u0000\u0000\u0000\u0005\u0001\u0000\u0000"+
		"\u0000\u0000\u0007\u0001\u0000\u0000\u0000\u0000\t\u0001\u0000\u0000\u0000"+
		"\u0000\u000b\u0001\u0000\u0000\u0000\u0000\r\u0001\u0000\u0000\u0000\u0000"+
		"\u000f\u0001\u0000\u0000\u0000\u0000\u0011\u0001\u0000\u0000\u0000\u0000"+
		"\u0013\u0001\u0000\u0000\u0000\u0000\u0015\u0001\u0000\u0000\u0000\u0000"+
		"\u0017\u0001\u0000\u0000\u0000\u0000\u0019\u0001\u0000\u0000\u0000\u0000"+
		"\u001b\u0001\u0000\u0000\u0000\u0000\u001d\u0001\u0000\u0000\u0000\u0000"+
		"\u001f\u0001\u0000\u0000\u0000\u0000!\u0001\u0000\u0000\u0000\u0000#\u0001"+
		"\u0000\u0000\u0000\u0000%\u0001\u0000\u0000\u0000\u0000\'\u0001\u0000"+
		"\u0000\u0000\u0000)\u0001\u0000\u0000\u0000\u0000+\u0001\u0000\u0000\u0000"+
		"\u0000-\u0001\u0000\u0000\u0000\u0000/\u0001\u0000\u0000\u0000\u00001"+
		"\u0001\u0000\u0000\u0000\u00003\u0001\u0000\u0000\u0000\u00005\u0001\u0000"+
		"\u0000\u0000\u00019\u0001\u0000\u0000\u0000\u0003A\u0001\u0000\u0000\u0000"+
		"\u0005C\u0001\u0000\u0000\u0000\u0007E\u0001\u0000\u0000\u0000\tG\u0001"+
		"\u0000\u0000\u0000\u000bI\u0001\u0000\u0000\u0000\rK\u0001\u0000\u0000"+
		"\u0000\u000fM\u0001\u0000\u0000\u0000\u0011O\u0001\u0000\u0000\u0000\u0013"+
		"Q\u0001\u0000\u0000\u0000\u0015S\u0001\u0000\u0000\u0000\u0017U\u0001"+
		"\u0000\u0000\u0000\u0019X\u0001\u0000\u0000\u0000\u001b[\u0001\u0000\u0000"+
		"\u0000\u001da\u0001\u0000\u0000\u0000\u001fk\u0001\u0000\u0000\u0000!"+
		"m\u0001\u0000\u0000\u0000#p\u0001\u0000\u0000\u0000%u\u0001\u0000\u0000"+
		"\u0000\'z\u0001\u0000\u0000\u0000)\u0084\u0001\u0000\u0000\u0000+\u009c"+
		"\u0001\u0000\u0000\u0000-\u009e\u0001\u0000\u0000\u0000/\u00a0\u0001\u0000"+
		"\u0000\u00001\u00ae\u0001\u0000\u0000\u00003\u00c3\u0001\u0000\u0000\u0000"+
		"5\u00c8\u0001\u0000\u0000\u00007\u00ce\u0001\u0000\u0000\u00009:\u0005"+
		"e\u0000\u0000:;\u0005s\u0000\u0000;<\u0005c\u0000\u0000<=\u0005r\u0000"+
		"\u0000=>\u0005e\u0000\u0000>?\u0005v\u0000\u0000?@\u0005e\u0000\u0000"+
		"@\u0002\u0001\u0000\u0000\u0000AB\u0005(\u0000\u0000B\u0004\u0001\u0000"+
		"\u0000\u0000CD\u0005)\u0000\u0000D\u0006\u0001\u0000\u0000\u0000EF\u0003"+
		"\u000b\u0005\u0000F\b\u0001\u0000\u0000\u0000GH\u0005+\u0000\u0000H\n"+
		"\u0001\u0000\u0000\u0000IJ\u0005-\u0000\u0000J\f\u0001\u0000\u0000\u0000"+
		"KL\u0005*\u0000\u0000L\u000e\u0001\u0000\u0000\u0000MN\u0005/\u0000\u0000"+
		"N\u0010\u0001\u0000\u0000\u0000OP\u0005%\u0000\u0000P\u0012\u0001\u0000"+
		"\u0000\u0000QR\u0005<\u0000\u0000R\u0014\u0001\u0000\u0000\u0000ST\u0005"+
		">\u0000\u0000T\u0016\u0001\u0000\u0000\u0000UV\u0005<\u0000\u0000VW\u0005"+
		"=\u0000\u0000W\u0018\u0001\u0000\u0000\u0000XY\u0005>\u0000\u0000YZ\u0005"+
		"=\u0000\u0000Z\u001a\u0001\u0000\u0000\u0000[\\\u0005i\u0000\u0000\\]"+
		"\u0005g\u0000\u0000]^\u0005u\u0000\u0000^_\u0005a\u0000\u0000_`\u0005"+
		"l\u0000\u0000`\u001c\u0001\u0000\u0000\u0000ab\u0005d\u0000\u0000bc\u0005"+
		"i\u0000\u0000cd\u0005f\u0000\u0000de\u0005e\u0000\u0000ef\u0005r\u0000"+
		"\u0000fg\u0005e\u0000\u0000gh\u0005n\u0000\u0000hi\u0005t\u0000\u0000"+
		"ij\u0005e\u0000\u0000j\u001e\u0001\u0000\u0000\u0000kl\u0005e\u0000\u0000"+
		"l \u0001\u0000\u0000\u0000mn\u0005o\u0000\u0000no\u0005u\u0000\u0000o"+
		"\"\u0001\u0000\u0000\u0000pq\u0005n\u0000\u0000qr\u0005a\u0000\u0000r"+
		"s\u0005o\u0000\u0000s$\u0001\u0000\u0000\u0000tv\u00037\u001b\u0000ut"+
		"\u0001\u0000\u0000\u0000vw\u0001\u0000\u0000\u0000wu\u0001\u0000\u0000"+
		"\u0000wx\u0001\u0000\u0000\u0000x&\u0001\u0000\u0000\u0000y{\u00037\u001b"+
		"\u0000zy\u0001\u0000\u0000\u0000{|\u0001\u0000\u0000\u0000|z\u0001\u0000"+
		"\u0000\u0000|}\u0001\u0000\u0000\u0000}~\u0001\u0000\u0000\u0000~\u0080"+
		"\u0005.\u0000\u0000\u007f\u0081\u00037\u001b\u0000\u0080\u007f\u0001\u0000"+
		"\u0000\u0000\u0081\u0082\u0001\u0000\u0000\u0000\u0082\u0080\u0001\u0000"+
		"\u0000\u0000\u0082\u0083\u0001\u0000\u0000\u0000\u0083(\u0001\u0000\u0000"+
		"\u0000\u0084\u0088\u0005\"\u0000\u0000\u0085\u0087\t\u0000\u0000\u0000"+
		"\u0086\u0085\u0001\u0000\u0000\u0000\u0087\u008a\u0001\u0000\u0000\u0000"+
		"\u0088\u0089\u0001\u0000\u0000\u0000\u0088\u0086\u0001\u0000\u0000\u0000"+
		"\u0089\u008b\u0001\u0000\u0000\u0000\u008a\u0088\u0001\u0000\u0000\u0000"+
		"\u008b\u008c\u0005\"\u0000\u0000\u008c*\u0001\u0000\u0000\u0000\u008d"+
		"\u008e\u0005v\u0000\u0000\u008e\u008f\u0005e\u0000\u0000\u008f\u0090\u0005"+
		"r\u0000\u0000\u0090\u0091\u0005d\u0000\u0000\u0091\u0092\u0005a\u0000"+
		"\u0000\u0092\u0093\u0005d\u0000\u0000\u0093\u0094\u0005e\u0000\u0000\u0094"+
		"\u0095\u0005i\u0000\u0000\u0095\u0096\u0005r\u0000\u0000\u0096\u009d\u0005"+
		"o\u0000\u0000\u0097\u0098\u0005f\u0000\u0000\u0098\u0099\u0005a\u0000"+
		"\u0000\u0099\u009a\u0005l\u0000\u0000\u009a\u009b\u0005s\u0000\u0000\u009b"+
		"\u009d\u0005o\u0000\u0000\u009c\u008d\u0001\u0000\u0000\u0000\u009c\u0097"+
		"\u0001\u0000\u0000\u0000\u009d,\u0001\u0000\u0000\u0000\u009e\u009f\u0005"+
		";\u0000\u0000\u009f.\u0001\u0000\u0000\u0000\u00a0\u00a1\u0005/\u0000"+
		"\u0000\u00a1\u00a2\u0005/\u0000\u0000\u00a2\u00a6\u0001\u0000\u0000\u0000"+
		"\u00a3\u00a5\t\u0000\u0000\u0000\u00a4\u00a3\u0001\u0000\u0000\u0000\u00a5"+
		"\u00a8\u0001\u0000\u0000\u0000\u00a6\u00a7\u0001\u0000\u0000\u0000\u00a6"+
		"\u00a4\u0001\u0000\u0000\u0000\u00a7\u00aa\u0001\u0000\u0000\u0000\u00a8"+
		"\u00a6\u0001\u0000\u0000\u0000\u00a9\u00ab\u0007\u0000\u0000\u0000\u00aa"+
		"\u00a9\u0001\u0000\u0000\u0000\u00ab\u00ac\u0001\u0000\u0000\u0000\u00ac"+
		"\u00ad\u0006\u0017\u0000\u0000\u00ad0\u0001\u0000\u0000\u0000\u00ae\u00af"+
		"\u0005/\u0000\u0000\u00af\u00b0\u0005*\u0000\u0000\u00b0\u00b4\u0001\u0000"+
		"\u0000\u0000\u00b1\u00b3\t\u0000\u0000\u0000\u00b2\u00b1\u0001\u0000\u0000"+
		"\u0000\u00b3\u00b6\u0001\u0000\u0000\u0000\u00b4\u00b5\u0001\u0000\u0000"+
		"\u0000\u00b4\u00b2\u0001\u0000\u0000\u0000\u00b5\u00b7\u0001\u0000\u0000"+
		"\u0000\u00b6\u00b4\u0001\u0000\u0000\u0000\u00b7\u00b8\u0005*\u0000\u0000"+
		"\u00b8\u00b9\u0005/\u0000\u0000\u00b9\u00bd\u0001\u0000\u0000\u0000\u00ba"+
		"\u00bc\u00033\u0019\u0000\u00bb\u00ba\u0001\u0000\u0000\u0000\u00bc\u00bf"+
		"\u0001\u0000\u0000\u0000\u00bd\u00bb\u0001\u0000\u0000\u0000\u00bd\u00be"+
		"\u0001\u0000\u0000\u0000\u00be\u00c0\u0001\u0000\u0000\u0000\u00bf\u00bd"+
		"\u0001\u0000\u0000\u0000\u00c0\u00c1\u0006\u0018\u0000\u0000\u00c12\u0001"+
		"\u0000\u0000\u0000\u00c2\u00c4\u0005\r\u0000\u0000\u00c3\u00c2\u0001\u0000"+
		"\u0000\u0000\u00c3\u00c4\u0001\u0000\u0000\u0000\u00c4\u00c5\u0001\u0000"+
		"\u0000\u0000\u00c5\u00c6\u0005\n\u0000\u0000\u00c64\u0001\u0000\u0000"+
		"\u0000\u00c7\u00c9\u0007\u0001\u0000\u0000\u00c8\u00c7\u0001\u0000\u0000"+
		"\u0000\u00c9\u00ca\u0001\u0000\u0000\u0000\u00ca\u00c8\u0001\u0000\u0000"+
		"\u0000\u00ca\u00cb\u0001\u0000\u0000\u0000\u00cb\u00cc\u0001\u0000\u0000"+
		"\u0000\u00cc\u00cd\u0006\u001a\u0000\u0000\u00cd6\u0001\u0000\u0000\u0000"+
		"\u00ce\u00cf\u0007\u0002\u0000\u0000\u00cf8\u0001\u0000\u0000\u0000\f"+
		"\u0000w|\u0082\u0088\u009c\u00a6\u00aa\u00b4\u00bd\u00c3\u00ca\u0001\u0006"+
		"\u0000\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}