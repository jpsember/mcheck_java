package mcheck;

import base.*;
import java.util.*;
import java.io.*;

public class mch
    extends EmApplication implements IScript {

  public static void main(String[] mainArgs) {

    new mch().doMain(mainArgs);

  }

  protected void doMain(String[] mainArgs) {
    if (false && mainArgs.length == 0) {
      String[] test = {
          "script.txt",
      };
      mainArgs = test;
    }
    super.doMain(mainArgs);
    try {
      // -b
      boolean showBuchi = false;
      // -r
      boolean printReduced = false;
      // -v
      boolean verbose = false;
      // -m
      boolean showMarkedCTL = false;
      // true if we're to read from stdIn
      boolean stdIn = true;
      // true if formulas have been entered
      boolean formulasDefined = false;
      // tokenizer
      DFA dfa = new DFA(Streams.openResource("mcheck.dfa"));

      Environment env = new Environment();
      env.dfa = dfa;

      Model model = new Model(env);

      String title =
          "--//  mch: Kripke Model Checker for LTL & CTL Formulas\n" +
          "-//        Written by Jeff Sember, Spring 2005\n" +
          "\n";

      String helpMsg =
          title +
          "Usage: mch <opts> {<input file>}*\n"
          +
          "  <input file>  : text file to read models, specifications from;\n"
          +
          "                      if none specified, uses standard input\n"
          + "<opts> include:\n"
          + " -b, --buchi    : show Buchi automata\n"
          + " -e, --echo     : echo input files\n"
          + " -h, --help     : print help\n"
          +
          " -m, --mark     : show formulas as they're marked in states (CTL only)\n"
          + " -p, --paren    : don't filter out unnecessary parentheses\n"
          + " -r, --reduced  : display reduced formulas\n"
          + " -v, --verbose  : verbose output\n";

      String defaults = " == --buchi -b --echo -e --help -h --mark -m"
          + " --paren -p --reduced -r --verbose -v ";

      base.Scanner scan = new base.Scanner(dfa, T_WHITESPACE);
      CmdArgs args = new CmdArgs(mainArgs, defaults, helpMsg);

      // command line options:
      while (args.hasNext()) {
        if (args.nextIsValue()) {
          stdIn = false;
          String p = args.nextValue();
          scan.include(Streams.reader(p), p, false);
          continue;
        }

        switch (args.nextChar()) {
          case 'b':
            showBuchi = true;
            break;
          case 'e':
            scan.setEcho(true);
            break;
          case 'v':
            verbose = true;
            break;
          case 'm':
            showMarkedCTL = true;
            break;
          case 'r':
            printReduced = true;
            break;
          case 'p':
            env.filterParen = true;
            break;
          default:
            args.unsupported();
        }
      }
      args.done();

      if (stdIn) {
        scan.include(Streams.reader(null), "");
      }

      LTLCheck c = null;
      c = new LTLCheck(env,
                       (verbose ? LTLCheck.OPT_PRINTSTATES : 0)
                       | (verbose ? LTLCheck.OPT_PRINTFULLSEQ : 0)
                       | (showBuchi ? LTLCheck.OPT_PRINTBUCHI : 0)
          );

      while (!scan.eof()) {

        Token t = scan.peek();
        // is it a model definition?
        if (t.id(T_MODELOP)) {
          model.clear();
          env.clear();
          formulasDefined = false;
          model.parse(scan);
          Streams.out.println("Parsed model, " + model.states() + " states\n");
          if (verbose) {
            model.print();
            Streams.out.println();
            formulasDefined = true;
          }
        }
        else if (t.id(T_COMPARE)) {

          // ? <form> : <form>
          // compare two LTL formulas

          formulasDefined = model.defined();
          scan.read();

          Formula f1 = new Formula(env, scan);
          scan.read(T_COMPAREMID);
          Formula f2 = new Formula(env, scan);

          c.compare(f1, f2, printReduced);
        }
        else {

          // assume it's a formula.

          formulasDefined = model.defined();

          Formula f = new Formula(env, scan);
          Streams.out.println(f);
          if (printReduced) {
            Streams.out.println(f.reduced());
          }

          if (!model.defined()) {
            continue;
          }

          // if a model has been defined,
          // check it against this formula.

          if (f.isCTL() && !f.isLTL()
              ) {
            CTLCheck c2 = new CTLCheck();
            BitSet sat = new BitSet();
            c2.check(env, model, f, sat, verbose, showMarkedCTL);

            // Verify that all start states satisfy the formula
            OrdSet is = model.initialStates();
            boolean first = true;
            for (int i = 0; i < is.length(); i++) {
              int iName = is.get(i);
              if (!sat.get(model.stateId(iName))) {
                if (first) {
                  Streams.out.print("Not satisfied; start states: ");
                  first = false;
                }
                else {
                  Streams.out.print(' ');
                }
                Streams.out.print(iName);
              }
            }
            if (first) {
              Streams.out.print("Satisified.");
            }
            Streams.out.print("\n\n");
            continue;
          }

          if (f.isLTL()) {
            c.check(model, f);
            continue;
          }

          Streams.out.println("(cannot check mixed CTL/LTL formula...)\n");
          continue;
        }
      }

      if (!formulasDefined && model.defined()) {
        model.print();
        Streams.out.println();
      }
    }
    catch (ScanException e) {
      Streams.out.println(e.toString());
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
