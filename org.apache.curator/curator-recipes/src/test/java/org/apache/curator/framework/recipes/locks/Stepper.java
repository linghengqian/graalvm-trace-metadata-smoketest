
package org.apache.curator.framework.recipes.locks;

class Stepper
{
    private int     available = 0;

    synchronized void        await() throws InterruptedException
    {
        while ( available == 0 )
        {
            wait();
        }
        --available;
        notifyAll();
    }

    synchronized void       countDown(int qty)
    {
        available += qty;
        notifyAll();
    }
}
