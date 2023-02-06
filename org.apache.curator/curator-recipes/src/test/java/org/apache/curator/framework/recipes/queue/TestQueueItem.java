
package org.apache.curator.framework.recipes.queue;

class TestQueueItem implements Comparable<TestQueueItem>
{
    final String    str;

    TestQueueItem(String str)
    {
        this.str = str;
    }

    @Override
    public int compareTo(TestQueueItem rhs)
    {
        if ( this == rhs )
        {
            return 0;
        }

        int         val = Integer.parseInt(str);
        int         rhsVal = Integer.parseInt(rhs.str);
        int         diff = val - rhsVal;
        return (diff < 0) ? -1 : ((diff > 0) ? 1 : 0);
    }

    @Override
    public boolean equals(Object o)
    {
        if ( this == o )
        {
            return true;
        }
        if ( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        TestQueueItem that = (TestQueueItem)o;

        return str.equals(that.str);

    }

    @Override
    public int hashCode()
    {
        return str.hashCode();
    }
}
