<?php
set_time_limit(10000);
for ($j = 40; $j < 390; ++$j)
{
    $html = file_get_contents('http://www.oizoioi.com.my/accessory/computer-accessories/&term=&website=&rating=&lower_price=-1&upper_price=1000000000&order=&outofstock=false&page='.$j);

    $links = explode("<span class=\"read_more\"><a href=\"", $html);

    for ($k = 2; $k < sizeof($links); ++$k)
    {
        $link = "http://www.oizoioi.com.my".substr($links[$k], 0, strpos($links[$k], '"'));
        echo $link."<br/>";

        $html2 = file_get_contents($link);
        
        $website = substr($html2, strpos($html2, "trackOutboundLink(this, 'Outbound Links','")+strlen("trackOutboundLink(this, 'Outbound Links','"));
        $website =  substr($website, 0, strpos($website, "'"));
        echo $website."<br/>";

        $id = substr($link, strrpos($link, "-")+1);
        $id = substr($id, 0, strpos($id, "."));
        echo $id."<br/>";

        for ($i = 0; $i < 5; ++$i)
        {
            echo $i;
            $url = 'http://www.oizoioi.com.my/CMS/addReview.php';
            $postData = array();
            $postData['name'] = 'courts';
            $postData['rating'] = '1';
            $postData['title'] ='courts';
            $postData['content'] ='Online Shopping, Malaysia, Kaspersky, Microsoft Office, iPhone 6, Starcraft, Brita, GPS, Kindle, Note 3, Samsung.Online shopping in Malaysia for Amazon Kindle, Papago GPS, software, books, DVDs, videos, electronics, computers, software, apparel & accessories, tools & hardware, sporting goods, beauty & personal care, broadband & dsl & just about anything else. price comparison, mobile phone price, camera price, laptop price, tablet price, tv price, headphone price, electronic price. Price comparison website for mobile phones, tablets, cameras, laptops, TV, Speakers, Headphones... at top reliable retailers and ecommerce sites in Malaysia. www.youbeli.com.';
            $postData['id'] = trim($id);
            $postData['website'] = trim($website);
            $ch = curl_init();
            curl_setopt($ch, CURLOPT_URL, $url);
            curl_setopt($ch, CURLOPT_RETURNTRANSFER,1);
            curl_setopt($ch, CURLOPT_POST, 1);
            curl_setopt($ch, CURLOPT_POSTFIELDS, $postData);
            $result = curl_exec($ch);
            curl_close($ch);
            echo " here<br/>";
        }

    }
}

?>


