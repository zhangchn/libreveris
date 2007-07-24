//----------------------------------------------------------------------------//
//                                                                            //
//                          O m r E x e c u t o r s                           //
//                                                                            //
//  Copyright (C) Herve Bitteur 2000-2007. All rights reserved.               //
//  This software is released under the GNU General Public License.           //
//  Contact author at herve.bitteur@laposte.net to report bugs & suggestions. //
//----------------------------------------------------------------------------//
//
package omr.util;

import omr.constant.Constant;
import omr.constant.ConstantSet;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class <code>OmrExecutors</code> handles the two pools of threads
 * provided to the omr package, each pool containing a number of threads
 * equal to the machine number of processors plus one.
 *
 * @author Herv&eacute Bitteur
 * @version $Id$
 */
public class OmrExecutors
{
    //~ Static fields/initializers ---------------------------------------------

    /** Usual logger utility */
    private static final Logger logger = Logger.getLogger(OmrExecutors.class);

    /** Specific application parameters */
    private static final Constants constants = new Constants();

    /** Number of processors available */
    private static final int cpuNb = Runtime.getRuntime()
                                            .availableProcessors() + 1;

    /** Pool with high priority */
    private static volatile ExecutorService highExecutor;

    /** Pool with low priority */
    private static volatile ExecutorService lowExecutor;

    //~ Constructors -----------------------------------------------------------

    /** Not meant to be instantiated */
    private OmrExecutors ()
    {
    }

    //~ Methods ----------------------------------------------------------------

    //-----------------//
    // getHighExecutor //
    //-----------------//
    /**
     * Return the (single) pool of high priority threads
     *
     * @return the high pool, allocated if needed
     */
    public static ExecutorService getHighExecutor ()
    {
        if (highExecutor == null) {
            synchronized (OmrExecutors.class) {
                if (highExecutor == null) {
                    highExecutor = Executors.newFixedThreadPool(
                        cpuNb + 1,
                        new HighFactory());
                }
            }
        }

        return highExecutor;
    }

    //----------------//
    // getLowExecutor //
    //----------------//
    /**
     * Return the (single) pool of low priority threads
     *
     * @return the low pool, allocated if needed
     */
    public static ExecutorService getLowExecutor ()
    {
        if (lowExecutor == null) {
            synchronized (OmrExecutors.class) {
                if (lowExecutor == null) {
                    lowExecutor = Executors.newFixedThreadPool(
                        cpuNb + 1,
                        new LowFactory());
                }
            }
        }

        return lowExecutor;
    }

    //-----------------//
    // getNumberOfCpus //
    //-----------------//
    /**
     * Report the number of processors available
     *
     * @return the number of CPUs
     */
    public static int getNumberOfCpus ()
    {
        return cpuNb;
    }

    //----------//
    // shutdown //
    //----------//
    public static void shutdown ()
    {
        if (lowExecutor != null) {
            logger.info("Shutting down low executors");
            shutdownAndAwaitTermination(lowExecutor);
        }

        if (highExecutor != null) {
            logger.info("Shutting down high executors");
            shutdownAndAwaitTermination(highExecutor);
        }
    }

    //----------------//
    // useParallelism //
    //----------------//
    /**
     * Report whether we should try to use parallelism as much as possible
     *
     * @return true for parallel
     */
    public static boolean useParallelism ()
    {
        return constants.useParallelism.getValue();
    }

    //-----------------------------//
    // shutdownAndAwaitTermination //
    //-----------------------------//
    private static void shutdownAndAwaitTermination (ExecutorService pool)
    {
        pool.shutdown(); // Disable new tasks from being submitted

        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(
                constants.graceDelay.getValue(),
                TimeUnit.SECONDS)) {
                // Cancel currently executing tasks
                pool.shutdownNow();

                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(
                    constants.graceDelay.getValue(),
                    TimeUnit.SECONDS)) {
                    logger.warning("Pool did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread()
                  .interrupt();
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    //---------//
    // Factory //
    //---------//
    private abstract static class Factory
        implements ThreadFactory
    {
        protected final ThreadGroup group;

        Factory ()
        {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup()
                    : Thread.currentThread()
                            .getThreadGroup();
        }

        @Implement(ThreadFactory.class)
        public Thread newThread (Runnable r)
        {
            Thread t = new Thread(group, r, getThreadName(), 0);

            if (t.isDaemon()) {
                t.setDaemon(false);
            }

            return t;
        }

        protected abstract String getThreadName ();
    }

    //-----------//
    // Constants //
    //-----------//
    private static final class Constants
        extends ConstantSet
    {
        Constant.Boolean useParallelism = new Constant.Boolean(
            true,
            "Should we use parallelism when we have several processors?");

        //
        Constant.Integer graceDelay = new Constant.Integer(
            "seconds",
            15,
            "Time to wait for terminating tasks");
    }

    //-------------//
    // HighFactory //
    //-------------//
    private static class HighFactory
        extends Factory
    {
        private final AtomicInteger highThreadNumber = new AtomicInteger(1);

        @Override
        public Thread newThread (Runnable r)
        {
            Thread t = super.newThread(r);

            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }

            return t;
        }

        @Override
        protected String getThreadName ()
        {
            return "high-thread-" + highThreadNumber.getAndIncrement();
        }
    }

    //------------//
    // LowFactory //
    //------------//
    private static class LowFactory
        extends Factory
    {
        private final AtomicInteger lowThreadNumber = new AtomicInteger(1);

        @Override
        public Thread newThread (Runnable r)
        {
            Thread t = super.newThread(r);

            if (t.getPriority() != Thread.MIN_PRIORITY) {
                t.setPriority(Thread.MIN_PRIORITY);
            }

            return t;
        }

        @Override
        protected String getThreadName ()
        {
            return "low-thread-" + lowThreadNumber.getAndIncrement();
        }
    }
}
